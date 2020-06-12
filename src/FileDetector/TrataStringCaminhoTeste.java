package FileDetector;

public class TrataStringCaminhoTeste {
	private final String FILE_DETECTOR = "FileDetector.csv";
	private final String FILE_MAPPING = "FileMapping.csv";
	
	public boolean diretorioTesteValido(String diretorio) {
		String pacoteTest = "src/test/java";
		if (diretorio.contains(pacoteTest)) {
			return true;
		} else {
			return false;
		}
	}
	public String removeFileString(String diretorio) {
		String aux = "";
		aux = diretorio.substring(6, diretorio.length());
		return aux;	
	}
	public String inverteDuplicaBarraURL(String diretorio) {
		
		// se não for windows, retorna a mesma string
		if (!RAIDEUtils.isWindowsPath()) return diretorio;
		
		int tam = diretorio.length();
		char[] dir = diretorio.toCharArray();

		String newDiretorio = "";
		
		for (int i = 0 ; i<tam; i ++) {
			if (dir[i] == '/') {
				newDiretorio += "\\";
			}
			else {
				newDiretorio += dir[i];
			}			
		} 
		return newDiretorio;
	}
	public String srcToNameProject(String src) {
		String aux = "";
		char[] dir = src.toCharArray();
		int posicao = 0;
		int tamanho = src.length();
		
		for (int i = tamanho - 1; i >= 0 ; i--) {
			if (dir[i] == '\\'){
				posicao = i;
				break;
			}					
		}
		aux = src.substring(posicao + 1, tamanho);
		return aux;	
	}
	public String getFILE_DETECTOR() {
		return FILE_DETECTOR;
	}
	public String getFILE_MAPPING() {
		return FILE_MAPPING;
	}

}
