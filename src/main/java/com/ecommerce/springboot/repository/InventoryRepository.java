package com.ecommerce.springboot.repository;

import com.ecommerce.springboot.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findBySku(String sku);

    List<InventoryItem> findByActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.id = :id")
    Optional<InventoryItem> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.sku = :sku")
    Optional<InventoryItem> findBySkuWithLock(@Param("sku") String sku);

    @Modifying
    @Query("UPDATE InventoryItem i SET i.availableQuantity = i.availableQuantity - :quantity, " +
            "i.reservedQuantity = i.reservedQuantity + :quantity WHERE i.id = :id")
    int reserveQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE InventoryItem i SET i.availableQuantity = i.availableQuantity + :quantity, " +
            "i.reservedQuantity = i.reservedQuantity - :quantity WHERE i.id = :id")
    int releaseQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
}


