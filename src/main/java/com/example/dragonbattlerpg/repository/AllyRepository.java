package com.example.dragonbattlerpg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.dragonbattlerpg.entity.Ally;



@Repository
public interface AllyRepository extends JpaRepository<Ally, Integer>{

}
