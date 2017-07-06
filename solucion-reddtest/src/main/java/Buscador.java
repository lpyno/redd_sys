
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
	
	private static String token = null;			// articulo a buscar
	private String dataSource = null;			// donde buscar
	private String pattern = null;
	private static Buscador buscador = null;
	private static Pattern p = null;
	private static FileReader fileReader = null;
	private static List<Object> rv= null;
	JsonObject jsonObject = null;
	
	public static String getToken() {
		return token;
	}

	public String getDataSource() {
		return dataSource;
	}

	public String getPattern() {
		return pattern;
	}

	private Buscador( String pattern, String dataSource ){
		
		p = Pattern.compile( pattern );

		this.pattern = pattern;
		this.dataSource = dataSource; 
		
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

		rv = new ArrayList<>();
	}
	
	public static Buscador getInstance( String pattern, String dataSource ){
		
		if ( buscador == null ){
			buscador = new Buscador( pattern , dataSource );	
		}
		return buscador;
	}

	public static Buscador getInstance(){
		
		return buscador;

	}
	
	public List<Object> buscarSemejantes( ) {
		
		solicitaTokenEntrada();
		
		JsonObject atributosToken = jsonObject.getJsonObject( token );
		
		if ( atributosToken == null ) {
			System.out.println( "Artículo no existe." );
			System.out.println( "Cerrando." );
			System.exit( -4 );
		}
	
		//System.out.println( atributosToken.toString() );
		
		return buscaAtributos( jsonObject , atributosToken );

		
	}
	
	private static List<Object> buscaAtributos(JsonObject universoBusqueda , JsonObject atributosEntrada ) {
		
		String tokenBase = "sku-"; // ?
		JsonObject atributosTemp = null;
		List<String> entrada = new ArrayList<>();
		List<String> aux = new ArrayList<>();
		int matches = 0;
		
		entrada = atributosComoLista( atributosEntrada );
		
		for ( int i = 1 ; ; i++ ){
			tokenBase += i;
			if ( null != ( atributosTemp = universoBusqueda.getJsonObject( tokenBase ) ) ){
				aux = atributosComoLista( atributosTemp );
				matches = comparaAtributos( entrada , aux );
				actualizaResultados( matches, tokenBase );
			} else {
				return rv;
			}
		}
		
	}
	
	private static void actualizaResultados(int matches, String tokenBase) {
	
		Map<Integer, Integer> topMatchesCount = new HashMap<>();
		Map<Integer, String> topMatchesKeys = new HashMap<>();
		
		// asdf
		
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
