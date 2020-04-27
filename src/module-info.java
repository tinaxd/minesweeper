module work.tinax.sanemine {
	requires javafx.controls;
	requires transitive javafx.graphics;
	requires javafx.base;
	
	opens work.tinax.sanemine to javafx.graphics;
}