public class Solucion {
	
	final static String rutaArchivoArticulos = "src\\main\\resources\\redd-test-data.json";
	
	public static void main(String[] args) {
		
		Buscador buscador = Buscador.getInstance( "sku-\\d{1,5}" , rutaArchivoArticulos );
		
		buscador.buscarSemejantes( 5 );
		
		System.exit( 0 );
		
	}
	
}
