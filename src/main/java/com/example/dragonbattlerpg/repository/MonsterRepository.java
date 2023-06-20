package com.example.dragonbattlerpg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.dragonbattlerpg.entity.Monster;



@Repository
public interface MonsterRepository extends JpaRepository<Monster, Integer>{

}
