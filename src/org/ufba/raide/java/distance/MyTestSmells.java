package org.ufba.raide.java.distance;

public class MyTestSmells {
	String tipoTestSmell;
	String metodo;
	String classe;
	int linha;
	String tipoRefatoracao;
	
	public MyTestSmells(String tipoTestSmell, String metodo, String classe, int linha, String tipoRefatoracao) {
		super();
		this.tipoTestSmell = tipoTestSmell;
		this.metodo = metodo;
		this.classe = classe;
		this.linha = linha;
		this.tipoRefatoracao = tipoRefatoracao;
	}
	public MyTestSmells() {
		super();
		this.tipoTestSmell = "";
		this.metodo = "";
		this.classe = "";
		this.linha = 0;
		this.tipoRefatoracao = "";
	}
	public String getTipoTestSmell() {
		return tipoTestSmell;
	}
	public void setTipoTestSmell(String tipoTestSmell) {
		this.tipoTestSmell = tipoTestSmell;
	}
	public String getMetodo() {
		return metodo;
	}
	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}
	public String getClasse() {
		return classe;
	}
	public void setClasse(String classe) {
		this.classe = classe;
	}
	public int getLinha() {
		return linha;
	}
	public void setLinha(int linha) {
		this.linha = linha;
	}
	public String getTipoRefatoracao() {
		return tipoRefatoracao;
	}
	public void setTipoRefatoracao(String tipoRefatoracao) {
		this.tipoRefatoracao = tipoRefatoracao;
	}	
	
	

}
