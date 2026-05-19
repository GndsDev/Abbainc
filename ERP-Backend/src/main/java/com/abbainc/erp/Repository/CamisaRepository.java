package com.abbainc.erp.Repository;

import com.abbainc.erp.Entity.Camisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CamisaRepository extends JpaRepository<Camisa, Long> {

    Optional<Camisa> findBySku(String sku);
}