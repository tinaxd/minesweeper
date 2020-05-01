package work.tinax.sanemine;

public interface AICallback {
	void beforeStart(Board board);
	boolean onStep(Board board, int currentStep, int nestLevel);
}
