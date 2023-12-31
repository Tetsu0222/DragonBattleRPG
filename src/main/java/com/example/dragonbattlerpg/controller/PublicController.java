package com.example.dragonbattlerpg.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.dragonbattlerpg.entity.Ally;
import com.example.dragonbattlerpg.entity.Monster;
import com.example.dragonbattlerpg.object.PlayerCharacter;
import com.example.dragonbattlerpg.repository.AllyRepository;
import com.example.dragonbattlerpg.repository.MonsterRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PublicController {
	
	//インジェクション
	private final AllyRepository allyRepository;
	private final MonsterRepository monsterRepository;
	
	
	private final Map< Integer , PlayerCharacter > playerCharacterMap = new HashMap<>();
	

	//TOP画面に対応
	@GetMapping( "/" )
	public ModelAndView Index( ModelAndView mv ) {
		
		mv.setViewName( "index" );
		
		//プレイアブルキャラクターとエネミーキャラクターの選択肢を提示
		final List<Ally> allyList = allyRepository.findAll();
		final List<Monster> enemyList = monsterRepository.findAll();
		
		mv.addObject( "allyList" , allyList );
		mv.addObject( "enemyList" , enemyList );
		
		return mv;
	}
	
	
	//バトルへ遷移
	@GetMapping( "/battle" )
	public ModelAndView battle( @RequestParam( name = "PLV1" ) Integer pid1 ,
								@RequestParam( name = "PLV2" ) Integer pid2 ,
								@RequestParam( name = "PLV3" ) Integer pid3 ,
								@RequestParam( name = "PLV4" ) Integer pid4 ,
								@RequestParam( name = "MLV1" ) Integer mid1 ,
								@RequestParam( name = "MLV2" ) Integer mid2 ,
								@RequestParam( name = "MLV3" ) Integer mid3 ,
								@RequestParam( name = "MLV4" ) Integer mid4 ,
								ModelAndView mv ) {
		
		mv.setViewName( "battle" );
		
		Integer coordinatePlayerCharacterId = 0;
		//Integer coordinateEnemyId = 4;
		
		//選択に応じたプレイアブルキャラクターのIdを格納
		List<Integer> repositoryPlayerCharacterIdList = Stream.of( pid1 , pid2 , pid3 , pid4 )
				.filter( s -> s > 0 )
				.collect( Collectors.toList() );
		
		/*
		//選択に応じたエネミーキャラクターのIdを格納
		List<Integer> repositoryEnemyIdList = Stream.of( mid1 , mid2 , mid3 , mid4 )
				.filter( s -> s > 0 )
				.collect( Collectors.toList() );
		
		*/
		
		for( Integer playerCharacterid : repositoryPlayerCharacterIdList ) {
			
			//プレイヤーの人数は4人まで
			if( coordinatePlayerCharacterId > 3 ) {
				throw new IllegalArgumentException();
			}
			
			//リファクタリング要素 インスタンス生成はインターフェースを設ける。
			//PlayerCharacterのコンストラクタはprivateへ改修予定
			PlayerCharacter playerCharacter = new PlayerCharacter( allyRepository.findById( playerCharacterid ).orElseThrow() , coordinatePlayerCharacterId );
			playerCharacterMap.put( coordinatePlayerCharacterId , playerCharacter );
			
			coordinatePlayerCharacterId++;
			
		}
		
		mv.addObject( "playerCharacterMap" , playerCharacterMap );
		
			
		return mv;
	}
	
	
	/*
	//通常攻撃を選択
	@GetMapping( "/attack/{key}" )
	public ModelAndView attack( @PathVariable( name = keys ) int key ,
								ModelAndView mv ) {
		
		mv.setViewName( BattleScreen );
		myKeys = key;
		session.setAttribute( ScreenMode , NormalAttack );
		
		return mv;
		
	}
	
	
	//通常攻撃のターゲット選択(敵）
	@GetMapping( "/target/attack/monster/{key}" )
	public ModelAndView attackTargetMonster( @PathVariable( name = keys ) int key ,
											 ModelAndView mv ) {
		
		mv.setViewName( BattleScreen );
		Battle battle = (Battle)session.getAttribute( BattleObject );
		battle.selectionAttack( myKeys , key );
		
		session.setAttribute( BattleObject , battle );
		session.setAttribute( ScreenMode , BeforeTurn );
		
		return mv;
	}
	
	
	//防御を選択
	@GetMapping( "/defense/{key}" )
	public ModelAndView defense( @PathVariable( name = keys ) int key ,
								 ModelAndView mv ) {
		
		mv.setViewName( BattleScreen );
		Battle battle = (Battle)session.getAttribute( BattleObject );
		battle.selectionDefense( key );
		
		return mv;
	}
	
	
	//戦闘開始
	@GetMapping( "/start" )
	public ModelAndView start( ModelAndView mv , Locale locale ) {
		
		//いつもの処理
		mv.setViewName( BattleScreen );
		Battle battle = (Battle)session.getAttribute( BattleObject );
		
		//前回までのログを消去
		battle.getMesageList().clear();
		
		//各キャラクターの行動順を規定
		battle.turn();
		
		//各キャラクターの座標を素早さが高い順（降順）でソートしたリストを取得
		List<Entry<Integer, Integer>> turnList = battle.getTurnList();
		
		//素早さで順でソートされたリストから、各キャラクターの座標だけ抽出してキューへ格納
		//このキューを用いて具体的な戦闘処理を実施する。
		this.turnqueue = TurnQueue.getTurnQueue( turnList );
		
		//ターンの最初に発動する効果を処理
		battle.startSkill();
		battle.getMesageList().add( turnCount + messageSource.getMessage( "turn.start" , null , locale ) );
		session.setAttribute( BattleObject , battle );
		session.setAttribute( ScreenMode   , TurnProgression );

		return mv;
	}
	
	
	//戦闘続行
	@GetMapping( "/next" )
	public ModelAndView next( ModelAndView mv , Locale locale ) {
		
		//いつもの処理
		mv.setViewName( BattleScreen );
		Battle battle = (Battle)session.getAttribute( BattleObject );
	
		//前回までのログを消去
		battle.getMesageList().clear();
		
		//素早さ順に行動
		this.turnAction( battle , locale );
		
		return mv;
	}
	
	
	//ターン終了
	@GetMapping( "/end" )
	public ModelAndView end( ModelAndView mv ) {
		
		//いつもの処理
		mv.setViewName( BattleScreen );
		Battle battle = (Battle)session.getAttribute( BattleObject );
		
		session.invalidate();
		session.setAttribute( BattleObject , battle );
		session.setAttribute( ScreenMode , BeforeTurn );
		
		return mv;
	}
	
	
	
	//------------------------------------------------------
	//素早さ順で行動処理を実行させるメソッド
	//別クラスへ委譲させたい。
	//------------------------------------------------------
	public void turnAction( Battle battle , Locale locale ) {
		
		if( turnqueue.peek() != null ) {
			
			Integer actionObj = turnqueue.poll();
			
			//ターン終了判定
			if( this.isPossible( battle , actionObj )){
				
				//判定結果trueであれば行動実行
				battle.startBattle( actionObj );
				
				//戦闘終了判定
				if( battle.getTargetSetAlly().size() == 0 ) {
					session.invalidate();
					battle.getMesageList().add( messageSource.getMessage( "lose.message" , null , locale ) );
					session.setAttribute( BattleObject , battle );
					session.setAttribute( ScreenMode , BattleResult );
					
				}else if( battle.getTargetSetEnemy().size() == 0 ) {
					session.invalidate();
					battle.getMesageList().add( messageSource.getMessage( "win.message" , null , locale ) );
					session.setAttribute( BattleObject , battle );
					session.setAttribute( ScreenMode , BattleResult );
					
				}else{
					session.invalidate();
					session.setAttribute( BattleObject , battle );
					session.setAttribute( ScreenMode , TurnProgression );
				}
			
			//全員の行動が終了
			}else{
				
				//ターン終了時に発動する処理
				battle.endSkill();
				battle.getMesageList().add( turnCount + messageSource.getMessage( "turn.end" , null , locale ) );
				this.turnCount += 1;
				
				session.invalidate();
				session.setAttribute( BattleObject , battle );
				session.setAttribute( ScreenMode   , TurnEnd  );
			}
			
		//全キャラクターの行動終了
		}else{
			
			//ターン終了時に発動する処理
			battle.endSkill();
			battle.getMesageList().add( turnCount + messageSource.getMessage( "turn.end" , null , locale ) );
			this.turnCount += 1;
			
			session.invalidate();
			session.setAttribute( BattleObject , battle );
			session.setAttribute( ScreenMode   , TurnEnd  );
		}
	}
	
	
	
	
	//-----------------------------------------------------
	//ターン継続判定を行うメソッド、別クラスへ委譲させたい。
	//再帰的に処理し、falseを返すとターン終了させる。
	//-----------------------------------------------------
	public boolean isPossible( Battle battle , Integer actionObj ) {
		
		boolean possible = false;
		
		//味方側の生存チェック
		if( battle.getPartyMap().get( actionObj ) != null ){
			
			//生存しているかどうかで処理を分岐
			if( battle.getPartyMap().get( actionObj ).getSurvival() == 0 ) {
				
				//行動対象者が死亡している場合は、該当インデックスを次の行動対象者で上書き
				if( turnqueue.peek() != null ) {
					actionObj = turnqueue.poll();
					
					//次の行動対象者も生存チェックを実行
					if( this.isPossible( battle , actionObj )) {
						possible = true;
					
					//自メソッドを繰り返し、結果的に値がなくなっていればターン終了判定(false)を返す。
					}else{
						possible = false;
					}
				
				//次の値が存在しなければターン終了(falseを返す)
				}else{
					possible = false;
				}
				
			//生存していれば処理実行
			}else{
				possible = true;
			}
			
		//敵側の生存チェック
		}else if( battle.getMonsterDataMap().get( actionObj ) != null ){
			
			if( battle.getMonsterDataMap().get( actionObj ).getSurvival() == 0 ) {
					
				//行動対象者が死亡している場合は、該当インデックスを次の行動対象者で上書き
				if( turnqueue.peek() != null ) {
					actionObj = turnqueue.poll();
						
					//次の行動対象者も生存チェックを実行
					if( this.isPossible( battle , actionObj )) {
						possible = true;
						
					//自メソッドを繰り返し、結果的に値がなくなっていればターン終了判定(false)を返す。
					}else{
						possible = false;
					}
					
				//次の値が存在しなければターン終了(falseを返す)
				}else{
					possible = false;
				}
					
			//生存していれば処理実行
			}else{
				possible = true;
			}
		}
		
		return possible;
	}
	*/
}
