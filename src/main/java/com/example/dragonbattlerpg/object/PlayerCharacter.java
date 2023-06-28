package com.example.dragonbattlerpg.object;

import java.util.Random;

import com.example.dragonbattlerpg.entity.Ally;

public class PlayerCharacter implements Characters{

	private final Integer playerCharacterId;
	private final MaxPlayerHitPoints MAX_HP;
	private final String name;
	private final Random random = new Random();
	
	private CurrentPlayerHitPoints HP;
	private CurrentPlayerOffensivePower ATK;
	private CurrentPlayerDefensePower DEF;
	private CurrentPlayerAgility SPD;
	private boolean survivalFlag;
	
	
	//nullの可能性あり。
	private EnemyCharacter targetEnemyCharacter;
	
	
	public PlayerCharacter( Ally ally , Integer id ){
		
		this.playerCharacterId = id;
		this.name = ally.getName();
		this.MAX_HP = new MaxPlayerHitPoints( ally.getHp() );
		this.HP = new CurrentPlayerHitPoints( ally.getHp() );
		this.ATK = new CurrentPlayerOffensivePower( ally.getAtk() );
		this.DEF = new CurrentPlayerDefensePower( ally.getDef() );
		this.SPD = new CurrentPlayerAgility( ally.getSpe() );
		this.survivalFlag = true;
	}
	
	public void healing( final CurrentPlayerHitPoints healingPoint ) {
		this.HP = HP.increasePlayerHitPoints( healingPoint , MAX_HP );
	}
	
	public void damage( final Integer damagePoint ) {
		
		//戦闘不能ならば呼び出されないように修正する予定
		if( !survivalFlag ) {
			return ;
		}
		
		if( HP.is_Dead() ) {
			throw new IllegalStateException( "キャラクターは戦闘不能なのに呼び出されています" );
		}
		
		final CurrentPlayerHitPoints resultDamage = DEF.defense( damagePoint );
		this.HP = HP.decreasePlayerHitPoints( resultDamage );
		
		if( HP.is_Dead() ) {
			this.survivalFlag = false;
		}
		
		this.displayAction( resultDamage );
	}
	
	public EnemyCharacter attak() {
		
		
		if( this.targetEnemyCharacter == null ) {
			throw new IllegalStateException( "対象が選択されていません。" );
		}
		
		//戦闘不能ならば呼び出されないように修正する予定
		if( !survivalFlag ) {
			return targetEnemyCharacter;
		}
		
		if( HP.is_Dead() ) {
			throw new IllegalStateException( "キャラクターは戦闘不能なのに呼び出されています" );
		}
		
		this.displayAction( ATK );
		return ATK.attack( targetEnemyCharacter );
	}
	
	public void displayAction( Points command ) {
		final StringBuilder message = new StringBuilder( name );
		message.append( command );
		System.out.println( message );
	}
	
	public int hashCode() {
		return playerCharacterId.hashCode();
	}
	
	public boolean equals( Object obj ) {
		return this.hashCode() == obj.hashCode();
	}
	
	public String toString() {
		return name;
	}
	
	public boolean is_Survival() {
		return this.survivalFlag;
	}
	
	public void targetEnemyCharacterSelection( final EnemyCharacter targetCharacter ) {
		this.targetEnemyCharacter = targetCharacter;
	}
	
	public Integer getSPD() {
		return this.SPD.getTurnSPD( random );
	}

	public String getHP() {
		return HP.toString();
	}

	public String getName() {
		return name;
	}
	
	
}