package com.rdm.repository;

import com.rdm.model.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {
    Page<Device> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT d FROM Device d WHERE d.isActive = true AND " +
            "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:protocol IS NULL OR d.protocol = :protocol) AND " +
            "(:status IS NULL OR d.status = :status)")
    Page<Device> searchDevices(
            @Param("name") String name,
            @Param("protocol") Device.Protocol protocol,
            @Param("status") Device.DeviceStatus status,
            Pageable pageable);

    Optional<Device> findByIdAndIsActiveTrue(Integer id);

    List<Device> findByProtocol(Device.Protocol protocol);

    List<Device> findByStatus(Device.DeviceStatus status);
}
