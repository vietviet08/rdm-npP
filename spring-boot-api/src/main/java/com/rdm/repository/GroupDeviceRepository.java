package com.rdm.repository;

import com.rdm.model.GroupDevice;
import com.rdm.model.GroupDeviceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupDeviceRepository extends JpaRepository<GroupDevice, GroupDeviceId> {
    List<GroupDevice> findByGroupId(Integer groupId);

    List<GroupDevice> findByDeviceId(Integer deviceId);

    Optional<GroupDevice> findByGroupIdAndDeviceId(Integer groupId, Integer deviceId);

    @Query("SELECT gd FROM GroupDevice gd WHERE gd.groupId IN " +
            "(SELECT gm.groupId FROM GroupMember gm WHERE gm.userId = :userId)")
    List<GroupDevice> findDevicesByUserGroups(@Param("userId") Integer userId);
}
