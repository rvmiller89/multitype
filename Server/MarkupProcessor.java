import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
/**
 * The MarkupProcessor has a list of FEUs received from the server and will
 * on it's own get the top item, update the rest of the vector with that item
 * then send it off to the other clients.
 * @author Rodrigo
 *
 */
public class MarkupProcessor {

	// Vector for the current FEUs
	private BlockingQueue<FrontEndUpdate> markupQueue;
	// Vector for history
	private Vector<FrontEndUpdate> markupHistory;
	private int currentRevision = 0; // TODO this assumes one file
	
	/**
	 * Constructor for MarkupProcessor
	 * Should spin off a new thread to control this class
	 * @param outs A list of the outputprocessor to output data to
	 */
	public MarkupProcessor(Vector<OutputProcessor> outs) {
		markupQueue = new ArrayBlockingQueue<FrontEndUpdate>(5000);
		markupHistory = new Vector<FrontEndUpdate>();
	}
	
	/**
	 * Adds an FEU to the end of the queue vector
	 * @param f The FrontEndUpdate to be added
	 */
	public void addFEU(FrontEndUpdate feu) {
		if(feu.getRevision() != currentRevision)
			updateReceivedFEU(feu);
		markupQueue.add(feu);
	}
	
	/**
	 * The received feu doesn't have the correct revision number
	 * we need to update it until it does
	 * @param f
	 */
	private void updateReceivedFEU(FrontEndUpdate f) {
		for(FrontEndUpdate old : markupHistory) {
			if(f.getRevision() < old.getRevision()) {
				updateFEUgivenFEU(f, old);
			}
		}		
	}

	/**
	 * Gets the next item in the markupQueue
	 */
	public FrontEndUpdate getTopItem() { //change back to private TODO
		FrontEndUpdate feu = null;
		try {
			feu = markupQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("mp here");
		updateMarkupQueue(feu);
		currentRevision++;
		feu.setRevision(currentRevision);
		if(markupHistory.size() == 100) {
			markupHistory.remove(markupHistory.lastElement());
			markupHistory.add(feu);
		}
		else {
			markupHistory.add(feu);
		}
		return feu;
	}
	
	/**
	 * Updates the rest of the markupQueue with the top queue
	 * @param topFeu 
	 */
	private void updateMarkupQueue(FrontEndUpdate topFeu) {
		for(FrontEndUpdate feu : markupQueue) {
			updateFEUgivenFEU(feu, topFeu);
		}
	}
	
	/**
	 * Updates an FEU given an FEU
	 * @param toUpdate
	 * @param given
	 */
	private void updateFEUgivenFEU (FrontEndUpdate toUpdate, 
			FrontEndUpdate given) {
		if(toUpdate == given) //don't update itself
			return;
		if(given.getMarkupType() == FrontEndUpdate.MarkupType.Insert) {
			int insertAt = given.getStartLocation();
			int sizeOfInsert = given.getInsert().length();
			if(toUpdate.getStartLocation() >= insertAt) {
				toUpdate.setStartLocation(toUpdate.getStartLocation()
						+sizeOfInsert);
				toUpdate.setEndLocation(toUpdate.getEndLocation()
						+sizeOfInsert);
				toUpdate.setRevision(toUpdate.getRevision()+1);
			}
		}
		else if(given.getMarkupType() == FrontEndUpdate.MarkupType.Delete) {
			int insertAt = given.getStartLocation();
			int sizeOfInsert = given.getEndLocation() - insertAt + 1;
			if(toUpdate.getStartLocation() >= insertAt) {
				toUpdate.setStartLocation(toUpdate.getStartLocation()
						-sizeOfInsert);
				toUpdate.setEndLocation(toUpdate.getEndLocation()
						-sizeOfInsert);
				toUpdate.setRevision(toUpdate.getRevision()+1);
			}
		}
		else { 
			// The markup doesn't affect other markups (cursor pos 
			// or highlight)
			return;
		}
	}

}
