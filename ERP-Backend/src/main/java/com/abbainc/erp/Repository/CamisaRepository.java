package com.abbainc.erp.Repository;

import com.abbainc.erp.Entity.Camisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CamisaRepository extends JpaRepository<Camisa, Integer> {
}