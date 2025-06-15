package com.bcastilho.example.device;

import com.bcastilho.example.device.admin.service.DeviceAdminService;
import com.bcastilho.example.device.admin.service.SelfTriggerService;
import com.example.model.Device;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.example.model.Device.StateEnum.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceApplicationTests {


    private final String STORE_NAME = "device-state-store";
    private final String INPUT_TOPIC = "devices";
    @Mock
    private StreamsBuilderFactoryBean factoryBean;
    @Mock
    private KafkaStreams kafkaStreams;
    @Mock
    private StreamsBuilder streamsBuilder;
    @Mock
    private KTable<String, Device> kTable;
    @Mock
    private ReadOnlyKeyValueStore<String, Device> store;
    @Mock
    private KeyValueIterator<String, Device> keyValueIterator;
    @Mock
    private KeyValueStore<String, Device> keyValueStore;
    @Mock
    private SelfTriggerService selfTriggerService;
    @InjectMocks
    private DeviceAdminService deviceAdminService;

    @BeforeEach
    void setUp() {
        deviceAdminService = new DeviceAdminService(
                mock(StoreBuilder.class),
                factoryBean,
                selfTriggerService
        );
        deviceAdminService.setKTable(kTable);
    }

    @Test
    void getDevice_ShouldReturnDevice_WhenExists() {
        // Arrange
        String deviceId = "device1";
        Device expectedDevice = createTestDevice(deviceId, "Test Device", "Brand1", AVAILABLE, null);

        when(factoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.store(any())).thenReturn(store);
        when(store.get(deviceId)).thenReturn(expectedDevice);

        // Act
        Device result = deviceAdminService.getDevice(deviceId);

        // Assert
        assertNotNull(result);
        assertEquals(deviceId, result.getId());
        verify(store).get(deviceId);
    }

    @Test
    void getDevice_ShouldThrowException_WhenStoreNotInitialized() {
        // Arrange
        when(factoryBean.getKafkaStreams()).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> deviceAdminService.getDevice("device1"));
    }

    @Test
    void getDevices_ShouldReturnAllDevices() {
        // Arrange
        List<Device> expectedDevices = Arrays.asList(
                createTestDevice("device1", "Device 1", "Brand1", AVAILABLE, null),
                createTestDevice("device2", "Device 2", "Brand2", IN_USE, null)
        );

        when(factoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.store(any())).thenReturn(store);
        when(store.all()).thenReturn(keyValueIterator);
        when(keyValueIterator.hasNext())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(keyValueIterator.next())
                .thenReturn(new KeyValue<>("device1", expectedDevices.get(0)))
                .thenReturn(new KeyValue<>("device2", expectedDevices.get(1)));

        // Act
        List<Device> result = deviceAdminService.getDevices();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsAll(expectedDevices));
    }

    private KeyValueIterator<String, Device> mockKeyValueIterator(List<Device> devices) {
        KeyValueIterator<String, Device> iterator = mock(KeyValueIterator.class);

        // Create an iterator for our test data
        Iterator<Device> deviceIterator = devices.iterator();

        // Set up the mock behavior
        when(iterator.hasNext()).thenAnswer(invocation -> deviceIterator.hasNext());

        when(iterator.next()).thenAnswer(invocation -> {
            Device device = deviceIterator.next();
            return new KeyValue<>(device.getId(), device);
        });

        return iterator;
    }

    @Test
    void getDevicesByBrand_ShouldReturnFilteredDevices() {
        // Arrange
        List<String> brands = List.of("Brand1", "Brand3");
        Device device1 = createTestDevice("device1", "Device 1", "Brand1", AVAILABLE, null);
        Device device2 = createTestDevice("device2", "Device 2", "Brand2", IN_USE, null);
        Device device3 = createTestDevice("device3", "Device 3", "Brand3", INACTIVE, null);

        when(factoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.store(any())).thenReturn(store);
        deviceAdminService.setKTable(kTable);

        List<Device> resultList = new ArrayList<>();
        resultList.add(createTestDevice("device1", "Device 1", "Brand1", AVAILABLE, null));
        resultList.add(createTestDevice("device2", "Device 2", "Brand2", IN_USE, null));
        resultList.add(createTestDevice("device3", "Device 3", "Brand3", INACTIVE, null));


        KeyValueIterator<String, Device> iterator = mockKeyValueIterator(resultList);


        when(store.all()).thenReturn(iterator);


        List<Device> result = deviceAdminService.getDevicesByBrand(brands);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.getId().equals("device1")));
        assertTrue(result.stream().anyMatch(d -> d.getId().equals("device3")));
        assertFalse(result.stream().anyMatch(d -> d.getId().equals("device2")));
    }

    @Test
    void addDevice_ShouldSetCorrectStateAndAction() {
        // Arrange
        Device newDevice = createTestDevice("new-device", "New Device", "BrandX", null, null);

        // Act
        Device result = deviceAdminService.addDevice(newDevice);

        // Assert
        assertEquals("addDevice", result.getAction());
        assertEquals(IN_PROCESS, result.getState());
        assertNotNull(result.getCreationTime());
        verify(selfTriggerService).postObject(eq("new-device"), any(Device.class));
    }

    @Test
    void updateDevice_ShouldValidateAndUpdate_WhenNotInUse() {
        // Arrange


        Device existingDevice = createTestDevice("device1", "Old Name", "Brand1", AVAILABLE, null);
        Device updatedDevice = createTestDevice("device1", "New Name", "Brand1", AVAILABLE, OffsetDateTime.now().plusDays(-1));

        when(factoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.store(any())).thenReturn(store);
        when(store.get("device1")).thenReturn(existingDevice);

        // Act
        Device result = deviceAdminService.updateDevice(updatedDevice);

        // Assert
        assertEquals("updateDevice", result.getAction());
        assertEquals(IN_PROCESS, result.getState());
        verify(selfTriggerService).postObject(eq("device1"), any(Device.class));
    }

    @Test
    void updateDevice_ShouldRejectNameChange_WhenInUse() {
        // Arrange
        Device existingDevice = createTestDevice("device1", "Old Name", "Brand1", IN_USE, null);
        Device updatedDevice = createTestDevice("device1", "New Name", "Brand1", IN_USE, null);

        when(factoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.store(any())).thenReturn(store);
        when(store.get("device1")).thenReturn(existingDevice);

        // Act
        Device result = deviceAdminService.updateDevice(updatedDevice);

        // Assert
        assertFalse(result.getMessage().isEmpty());
        assertTrue(result.getMessage().get(0).contains("Can not update device name"));
        assertEquals("Old Name", existingDevice.getName()); // Name shouldn't change
    }

    @Test
    void deleteDevice_ShouldReject_WhenInUse() {
        // Arrange
        Device device = createTestDevice("device1", "Device 1", "Brand1", IN_USE, null);

        when(factoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.store(any())).thenReturn(store);
        when(store.get("device1")).thenReturn(device);

        // Act
        Device result = deviceAdminService.deleteDeviceById("device1");

        // Assert
        assertFalse(result.getMessage().isEmpty());
        assertTrue(result.getMessage().get(0).contains("Can not delete device In Use"));
        verify(selfTriggerService, never()).postObject(any(), any());
    }

    @Test
    void deleteDevice_ShouldProceed_WhenNotInUse() {
        // Arrange
        Device device = createTestDevice("device1", "Device 1", "Brand1", AVAILABLE, null);

        when(factoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.store(any())).thenReturn(store);
        when(store.get("device1")).thenReturn(device);

        // Act
        Device result = deviceAdminService.deleteDeviceById("device1");

        // Assert
        assertEquals("deleteDevice", result.getAction());
        assertEquals(IN_PROCESS, result.getState());
        verify(selfTriggerService).postObject(eq("device1"), any(Device.class));
    }


    private Device createTestDevice(String id, String name, String brand, Device.StateEnum state, OffsetDateTime creationDate) {
        Device device = new Device();
        device.setId(id);
        device.setName(name);
        device.setBrand(brand);
        if (creationDate == null) {
            device.setCreationTime(OffsetDateTime.now());
        }
        if (state != null) {
            device.setState(state);
        }
        return device;
    }
}

