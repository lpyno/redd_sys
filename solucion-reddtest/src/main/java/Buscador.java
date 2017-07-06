
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class Buscador {
	
	final static int MIN_RESULTADOS = 1;
	final static int MAX_RESULTADOS = 50;		// untested
	
	private static String token = null;			// articulo a buscar
	private static String dataSource = null;	// donde buscar
	private static String pattern = null;
	private static Buscador buscador = null;
	private static Pattern p = null;
	private static FileReader fileReader = null;
	private static List<String> resultList= null;
	private static List<Integer> topMatchesCount = null;
	
	JsonObject jsonObject = null;
	private static int numeroResultados = MIN_RESULTADOS;
	
	public static String getToken() {
		return token;
	}

	public String getDataSource() {
		return dataSource;
	}

	public String getPattern() {
		return pattern;
	}

	private Buscador( String pattern, String dataSource ) {
		
		p = Pattern.compile( pattern );

		Buscador.pattern = pattern;
		Buscador.dataSource = dataSource; 
		
		if ( null == ( fileReader = getFileReader( dataSource ) ) ){
			System.out.println( "Cerrando." );
			System.exit( -2 );
		}
		
		JsonReader jsonReader = Json.createReader( fileReader );
		jsonObject = jsonReader.readObject();
		jsonReader.close();
		
		try {
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println( "Cerrando." );
			System.exit( -3 );
		}

	}
	
	public static Buscador getInstance( String pattern, String dataSource ) {
		
		if ( buscador == null ){
			buscador = new Buscador( pattern , dataSource );	
		}
		return buscador;
	}
 
	public static Buscador getInstance() {
		
		return buscador;

	}
	
	public void buscarSemejantes( int numeroResultadosRequerido ) {

		if ( numeroResultadosRequerido > MIN_RESULTADOS && numeroResultadosRequerido < MAX_RESULTADOS ) {
			numeroResultados = numeroResultadosRequerido;
		} else {
			System.out.println( "El numero de resultados solicitado es invalido." );
			System.out.println( "Se retornara el numero de resultados por defecto" );
		}
		
		solicitaTokenEntrada();
		
		// inicializa listas de numero de coincidencias y tokens
		topMatchesCount = new ArrayList<>();
		resultList = new ArrayList<>();
		for ( int i = 0 ; i < numeroResultados ; i++ ) {
			topMatchesCount.add( 0 );
			resultList.add( "empty" );
		}
		

		JsonObject atributosToken = jsonObject.getJsonObject( token );
		
		if ( atributosToken == null ) {
			System.out.println( "Artículo no existe." );
			System.out.println( "Cerrando." );
			System.exit( -4 );
		}
		//System.out.println( atributosToken.toString() );
		buscaAtributos( jsonObject , atributosToken );
	
		mostrarResultados();
		
	}
	
	private void mostrarResultados() {
		
		for( int i = 0 ; i < numeroResultados ; i++ ){
			System.out.println( ( i + 1 ) + ".- Coincidencias en atributos: " + topMatchesCount.get( i ) );
			String token = resultList.get( i );
			System.out.println( token + " " + jsonObject.get( token ).toString() );
		}
		return;
	}

	private static void buscaAtributos(JsonObject universoBusqueda , JsonObject atributosEntrada ) {
		
		String tokenBase = "sku-"; // ?
		String tokenToSearch = null ; // ?
		JsonObject atributosTemp = null;
		List<String> entrada = new ArrayList<>();
		List<String> aux = new ArrayList<>();
		int matches = 0;
		
		entrada = atributosComoLista( atributosEntrada );
		
		for ( int i = 1 ; ; i++ ){
			tokenToSearch = tokenBase + i;
			if ( null != ( atributosTemp = universoBusqueda.getJsonObject( tokenToSearch ) ) ){
				aux = atributosComoLista( atributosTemp );
				matches = comparaAtributos( entrada , aux );
				actualizaResultados( matches, tokenToSearch );
			} else {
				return;
			}
		}
		
	}
	
	private static void actualizaResultados( int matches , String tokenBase ) {
	
		for ( int i = 0 ; i < numeroResultados ; i++ ){
			if ( matches > topMatchesCount.get( i ) ){
				topMatchesCount.add( i , matches );
				topMatchesCount.remove( topMatchesCount.size() - 1 );
				resultList.add( i , tokenBase );
				resultList.remove( resultList.size() - 1 );
				break;
			}
		}
		// mantiene el largo de las listas igual al numero de resultados requerido
/*		if ( topMatchesCount.size() > numeroResultados ){
			topMatchesCount = topMatchesCount.subList( 0 , numeroResultados );
		}
		
		if ( resultList.size() > numeroResultados ){
			resultList = resultList.subList( 0 , numeroResultados );
		}
*/		
	}

	private static int comparaAtributos(List<String> entrada, List<String> aux) {
		
		int numeroAtributos = 0;
		int a = entrada.size(), b = aux.size();
		int atributosIguales = 0;
		
		if ( a == b )
			numeroAtributos = a;
		else
			numeroAtributos = Math.min(a, b);
		
		for ( int i = 0 ; i < numeroAtributos ; i++ ) {
			if ( entrada.get( i ).equals( aux.get( i ) ) )
				atributosIguales++;
		}
		
		return atributosIguales;
	}

	private static List<String> atributosComoLista( JsonObject atributos ) {
		
		String scratchPad = atributos.toString();
		return Arrays.asList( scratchPad.substring( 1, scratchPad.length() - 1 ).split(",") );
		
	}
	
	private static String solicitaTokenEntrada() {
		
		Scanner sc = new Scanner ( System.in );
		boolean entradaValida = false;
		int reintentos = 3;
		
		do {
			System.out.println( "Ingrese artículo a buscar: " );
			
			try {
				token = sc.next( p );
				entradaValida = true;
			} catch ( InputMismatchException e ) {
				System.out.println( e.toString() );
				System.out.println( "Articulo inválido. Ejemplos válidos: sku-1, sku-9999999." );
				reintentos--;
				sc.nextLine();
			}
			
		} while ( !entradaValida && ( reintentos > 0 ) );
		
		sc.close();
		return token;
	}
	
	private static FileReader getFileReader( String src ){
		
		try { 
			return new FileReader( src );	
		} catch ( FileNotFoundException e ) {
			System.out.println( e.getMessage() );
			return null;
		} 
	}

}
