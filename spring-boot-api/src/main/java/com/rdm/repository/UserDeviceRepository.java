package com.rdm.repository;

import com.rdm.model.UserDevice;
import com.rdm.model.UserDeviceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UserDeviceId> {
    List<UserDevice> findByUserId(Integer userId);

    List<UserDevice> findByDeviceId(Integer deviceId);

    Optional<UserDevice> findByUserIdAndDeviceId(Integer userId, Integer deviceId);

    @Query("SELECT ud FROM UserDevice ud JOIN ud.device d WHERE ud.userId = :userId AND d.isActive = true")
    List<UserDevice> findActiveDevicesByUserId(@Param("userId") Integer userId);

    @Query("SELECT ud FROM UserDevice ud WHERE ud.userId = :userId AND ud.deviceId = :deviceId")
    Optional<UserDevice> findUserDeviceAccess(@Param("userId") Integer userId, @Param("deviceId") Integer deviceId);

    void deleteByUserIdAndDeviceId(Integer userId, Integer deviceId);
}
