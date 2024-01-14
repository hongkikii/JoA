package com.mjuAppSW.joA.domain.room;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r Where r.status = :status")
    List<Room> findByStatus(@Param("status") String status);

    @Query("SELECT r FROM Room r WHERE r.status = :status AND r.id Not IN :roomIds")
    List<Room> findByStatusAndRoomIds(@Param("status") String status, @Param("roomIds") Set<Long> roomIds);
}

