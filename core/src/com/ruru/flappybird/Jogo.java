package com.ruru.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.graalvm.compiler.core.common.type.ArithmeticOpTable;

import java.util.Random;

public class Jogo extends ApplicationAdapter {
	//texturas
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture background;
	private Texture canoBaixo, canoTopo;
	private Texture gameOver;

	//formas para colisão
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima, retanguloCanoBaixo;

	//atributos de configuração
	private float larguraDispositivo, alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 0;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;

	//exibição de textos e atributos de pontuação
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;

	//configuração dos sons
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	//salvar pontuacao
	Preferences preferencias;

	//objetos para a camera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDGHT = 720;
	private final float VITURAL_HEIGHT = 1280;

	@Override
	public void create () {
		inicializarTexturas();
		inicializarObjetos();
	}

	@Override
	public void render () {

		//limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharObjetosTela();
		detectarColisoes();
	}

	private void verificarEstadoJogo(){

		boolean toqueTela = Gdx.input.justTouched();

		if(estadoJogo == 0){//passaro parado

			//aplica evento de toque na tela
			if (toqueTela){
				gravidade = -13;
				estadoJogo = 1;
				somVoando.play();
			}

		}else if(estadoJogo == 1){//começa o jogo

			//aplica evento de toque na tela
			if (toqueTela){
				gravidade = -13;
				somVoando.play();
			}

			//movimentar o cano
			int variacaoDoEspaco = random.nextInt(800) - 400;
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 400;

			if (posicaoCanoHorizontal < -canoTopo.getWidth()){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = variacaoDoEspaco;
				passouCano = false;
			}

			//aplicar gravidade no pássaro
			if(posicaoInicialVerticalPassaro > 0 || toqueTela)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			gravidade++;

		}else if (estadoJogo == 2){//colidiu com um cano

			if(pontos > pontuacaoMaxima){
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
			}

			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

			if (toqueTela){
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo/2;
				posicaoCanoHorizontal = larguraDispositivo;
			}
		}
	}

	private void detectarColisoes(){

		circuloPassaro.set((50 + posicaoHorizontalPassaro + passaros[0].getWidth()/2),
							(posicaoInicialVerticalPassaro + passaros[0].getHeight()/2),
						passaros[0].getWidth()/2);

		retanguloCanoBaixo.set(posicaoCanoHorizontal,
				(alturaDispositivo / 2 - (canoBaixo.getHeight()) - (espacoEntreCanos/2) + posicaoCanoVertical),
				canoBaixo.getWidth(), canoBaixo.getHeight());

		retanguloCanoCima.set(posicaoCanoHorizontal,
				(alturaDispositivo / 2 + (espacoEntreCanos/2) + posicaoCanoVertical),
				canoTopo.getWidth(), canoTopo.getHeight());

		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);

		if(colidiuCanoCima || colidiuCanoBaixo){
			if(estadoJogo == 1){
				somColisao.play();
				estadoJogo = 2;
			}
		}

		/* teste para desenhar as formas visualmente
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.RED);

		//passaro
		shapeRenderer.circle((50 + passaros[0].getWidth()/2), (posicaoInicialVerticalPassaro + passaros[0].getHeight()/2), passaros[0].getWidth()/2);

		//cano topo
		shapeRenderer.rect(posicaoCanoHorizontal,
				(alturaDispositivo / 2 + (espacoEntreCanos/2) + posicaoCanoVertical),
				canoTopo.getWidth(), canoTopo.getHeight());

		//cano baixo
		shapeRenderer.rect(posicaoCanoHorizontal,
				(alturaDispositivo / 2 - (canoBaixo.getHeight()) - (espacoEntreCanos/2) + posicaoCanoVertical),
				canoBaixo.getWidth(), canoBaixo.getHeight());

		shapeRenderer.end();
		 */
	}

	private void desenharObjetosTela(){
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		//desenhar o passaro
		batch.draw(background, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(passaros[(int) variacao], 50 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);

		//desenhar os canos
		batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo / 2 - (canoBaixo.getHeight()) - (espacoEntreCanos/2) + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal, alturaDispositivo / 2 + (espacoEntreCanos/2) + posicaoCanoVertical);

		//desenhar pontuacao
		textoPontuacao.draw(batch, String.valueOf(pontos), (larguraDispositivo/2)-(20), alturaDispositivo - (110));

		//desenhar gameover
		if(estadoJogo == 2){
			batch.draw(gameOver, (larguraDispositivo/2 - (gameOver.getWidth()/2)), alturaDispositivo/2);
			textoReiniciar.draw(batch, "Toque para reiniciar!",
								(larguraDispositivo/2 - 200), alturaDispositivo/2 - (gameOver.getHeight()/2));

			textoMelhorPontuacao.draw(batch, "Seu recorde é: " + pontuacaoMaxima + " pontos",
									(larguraDispositivo/2 - 235), alturaDispositivo/2 - (gameOver.getHeight()));
		}

		batch.end();
	}

	private void inicializarTexturas(){
		passaros = new Texture[3];

		//textura do passaro
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		//textura do background
		background = new Texture("fundo.png");

		//textura dos canos
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");

		//textura gameover
		gameOver = new Texture("game_over.png");
	}

	private void inicializarObjetos(){
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDGHT;
		alturaDispositivo = VITURAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 200;

		//configurações dos textos
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(3);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(3);

		//formas geométricas para colisões
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoCima = new Rectangle();
		retanguloCanoBaixo = new Rectangle();

		//inicializa os sons
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		//configura preferencias dos objetos
		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);

		//configuração da camera
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDGHT/2, VITURAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDGHT, VITURAL_HEIGHT, camera);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	public void validarPontos(){

		if(posicaoCanoHorizontal < 50-passaros[0].getWidth()){//cano passou da posição do pássaro
			if (!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		variacao += Gdx.graphics.getDeltaTime() * 10;
		//verifica variação para bater asas do pássaro
		if(variacao > 3)
			variacao = 0;
	}

	@Override
	public void dispose () {

	}
}
