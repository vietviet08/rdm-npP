package com.rdm.repository;

import com.rdm.model.GroupMember;
import com.rdm.model.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {
    List<GroupMember> findByUserId(Integer userId);

    List<GroupMember> findByGroupId(Integer groupId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.userId = :userId")
    List<GroupMember> findUserGroups(@Param("userId") Integer userId);
}
