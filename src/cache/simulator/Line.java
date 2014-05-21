
package cache.simulator;

/**
 *
 * @author Edilson
 */
public class Line {

    private boolean modified;
    private int memoryStart;
    private int memoryEnd;
    
    public Line() {
        this.modified = false;
        this.memoryStart = 0;
        this.memoryEnd = 0;
    }
    
    public boolean isValid() {
        return !(memoryStart == 0 && memoryEnd == 0);
    }
    
    public boolean isModified() {
        return this.modified;
    }
    
    public void setModified(boolean val) {
        this.modified = val;
    }

    public int getMemoryStart() {
        return memoryStart;
    }

    public void setMemoryStart(int memoryStart) {
        this.memoryStart = memoryStart;
    }

    public int getMemoryEnd() {
        return memoryEnd;
    }

    public void setMemoryEnd(int memoryEnd) {
        this.memoryEnd = memoryEnd;
    }
   
}
