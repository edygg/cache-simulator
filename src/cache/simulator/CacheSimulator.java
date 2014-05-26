package cache.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author Edilson
 */
public class CacheSimulator {

    public static final int RAM_CAPACITY = 4096;
    public static int[] ram = new int[RAM_CAPACITY];

    public static final int CACHE_LINES = 64;
    public static final Line[] cache = new Line[CACHE_LINES];

    public static final int BLOCK_LENGTH = 8;
    public static final int SETS = 16;
    public static final int SET_LINES = 4;

    public static final byte NO_CACHE = 0;
    public static final byte DIRECT = 1;
    public static final byte ASSOCIATIVE = 2;
    public static final byte SET_ASSOCIATIVE = 3;

    public static int associativePointer = 0;
    public static int[] setAssociativePointers;

    public static double time;

    public static void main(String[] args) {
        //fillRam();
        resetTime();
        test(NO_CACHE);
        System.out.printf("%.2f\n", time);
        resetTime();
        test(DIRECT);
        System.out.printf("%.2f\n", time);
        resetTime();
        test(ASSOCIATIVE);
        System.out.printf("%.2f\n", time);
        resetTime();
        test(SET_ASSOCIATIVE);
        System.out.printf("%.2f\n", time);
    }

    public static void fillRam() {
        try {
            Scanner dataFile = new Scanner(new File("./datos.txt"));
            for (int i = 0; i < ram.length; i++) {
                ram[i] = Integer.parseInt(dataFile.nextLine());
            }
        } catch (FileNotFoundException ex) {
            System.err.println("El archivo no se encuentra");
        }
    }

    public static void resetCache() {
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new Line();
        }
        associativePointer = 0;
        setAssociativePointers = new int[16];
    }

    public static void resetTime() {
        time = 0;
        resetCache();
    }

    public static int read(byte type, int pos) {
        if (type == NO_CACHE) {
            time += 0.1;
            return ram[pos];
        } else if (type == DIRECT) {
            int line = (pos / BLOCK_LENGTH) % CACHE_LINES;

            if (cache[line].isValid()) {
                if (pos >= cache[line].getMemoryStart() && pos <= cache[line].getMemoryEnd()) {
                    time += 0.01;
                    return ram[pos];
                } else {
                    if (cache[line].isModified()) {
                        cache[line].setMemoryStart((pos / BLOCK_LENGTH) * BLOCK_LENGTH);
                        cache[line].setMemoryEnd(cache[line].getMemoryStart() + BLOCK_LENGTH - 1);
                        cache[line].setModified(false);
                        time += 0.66 + 0.66 + 0.01;
                        return ram[pos];
                    } else {
                        cache[line].setMemoryStart((pos / BLOCK_LENGTH) * BLOCK_LENGTH);
                        cache[line].setMemoryEnd(cache[line].getMemoryStart() + BLOCK_LENGTH - 1);
                        cache[line].setModified(false);
                        time += 0.66 + 0.01;
                        return ram[pos];
                    }
                }
            } else {
                cache[line].setMemoryStart((pos / BLOCK_LENGTH) * BLOCK_LENGTH);
                cache[line].setMemoryEnd(cache[line].getMemoryStart() + BLOCK_LENGTH - 1);
                cache[line].setModified(false);
                time += 0.66 + 0.01;
                return ram[pos];
            }
        } else if (type == ASSOCIATIVE) {
            for (int i = 0; i < cache.length; i++) {
                if (cache[i].isValid()) {
                    if (pos >= cache[i].getMemoryStart() && pos <= cache[i].getMemoryEnd()) {
                        time += 0.01;
                        return ram[pos];
                    }
                }
            }

            if (cache[associativePointer].isValid()) {
                if (cache[associativePointer].isModified()) {
                    time += 0.66 + 0.66 + 0.01;
                } else {
                    time += 0.66 + 0.01;
                }
            } else {
                time += 0.66 + 0.01;
            }

            cache[associativePointer].setMemoryStart((pos / BLOCK_LENGTH) * BLOCK_LENGTH);
            cache[associativePointer].setMemoryEnd(cache[associativePointer].getMemoryStart() + BLOCK_LENGTH - 1);
            cache[associativePointer].setModified(false);
            associativePointer++;
            if (associativePointer >= CACHE_LINES) {
                associativePointer = 0;
            }
            return ram[pos];
        } else {
            int set = (pos / BLOCK_LENGTH) % SETS;

            for (int i = set * SET_LINES; i < set * SET_LINES + SET_LINES; i++) {
                if (cache[i].isValid()) {
                    if (pos >= cache[i].getMemoryStart() && pos <= cache[i].getMemoryEnd()) {
                        time += 0.01;
                        return ram[pos];
                    }
                }
            }
            
            if (cache[setAssociativePointers[set] + (set * SET_LINES)].isValid()) {
                if (cache[setAssociativePointers[set] + (set * SET_LINES)].isModified()) {
                    time += 0.66 + 0.66 + 0.01;
                } else {
                    time += 0.66 + 0.01;
                }
            } else {
                time += 0.66 + 0.01;
            }
            
            cache[setAssociativePointers[set] + (set * SET_LINES)].setMemoryStart((pos / BLOCK_LENGTH) * BLOCK_LENGTH);
            cache[setAssociativePointers[set] + (set * SET_LINES)].setMemoryEnd(cache[setAssociativePointers[set] + (set * SET_LINES)].getMemoryStart() + BLOCK_LENGTH - 1);
            cache[setAssociativePointers[set] + (set * SET_LINES)].setModified(false);
            setAssociativePointers[set]++;
            if (setAssociativePointers[set] >= SET_LINES) {
                setAssociativePointers[set] = 0;
            }
                
            return ram[pos];
        }
    }

    public static void write(byte type, int pos, int data) {
        if (type == NO_CACHE) {
            time += 0.1;
            ram[pos] = data;
        } else if (type == DIRECT) {
            int line = (pos / BLOCK_LENGTH) % CACHE_LINES;

            if (cache[line].isValid()) {
                if (pos >= cache[line].getMemoryStart() && pos <= cache[line].getMemoryEnd()) {
                    time += 0.01;
                    cache[line].setModified(true);
                    ram[pos] = data;
                } else {
                    if (cache[line].isModified()) {
                        cache[line].setMemoryStart((pos / BLOCK_LENGTH) * BLOCK_LENGTH);
                        cache[line].setMemoryEnd(cache[line].getMemoryStart() + BLOCK_LENGTH - 1);
                        cache[line].setModified(true);
                        time += 0.66 + 0.66 + 0.01;
                        ram[pos] = data;
                    } else {
                        cache[line].setMemoryStart((pos / BLOCK_LENGTH) * BLOCK_LENGTH);
                        cache[line].setMemoryEnd(cache[line].getMemoryStart() + BLOCK_LENGTH - 1);
                        cache[line].setModified(true);
                        time += 0.66 + 0.01;
                        ram[pos] = data;
                    }
                }
            } else {
                cache[line].setMemoryStart((pos / BLOCK_LENGTH) * BLOCK_LENGTH);
                cache[line].setMemoryEnd(cache[line].getMemoryStart() + BLOCK_LENGTH - 1);
                cache[line].setModified(true);
                time += 0.66 + 0.01;
                ram[pos] = data;
            }
        } else if (type == ASSOCIATIVE) {
            for (int i = 0; i < cache.length; i++) {
                if (cache[i].isValid()) {
                    if (pos >= cache[i].getMemoryStart() && pos <= cache[i].getMemoryEnd()) {
                        time += 0.01;
                        ram[pos] = data;
                        cache[i].setModified(true);
                        return;
                    }
                }
            }

            if (cache[associativePointer].isValid()) {
                if (cache[associativePointer].isModified()) {
                    time += 0.66 + 0.66 + 0.01;
                } else {
                    time += 0.66 + 0.01;
                }
            } else {
                time += 0.66 + 0.01;
            }

            cache[associativePointer].setModified(true);
            cache[associativePointer].setMemoryStart((pos / BLOCK_LENGTH) * BLOCK_LENGTH);
            cache[associativePointer].setMemoryEnd(cache[associativePointer].getMemoryStart() + BLOCK_LENGTH - 1);

            associativePointer++;

            if (associativePointer >= CACHE_LINES) {
                associativePointer = 0;
            }
            ram[pos] = data;
        } else {
            int set = (pos / BLOCK_LENGTH) % SETS;

            for (int i = set * SET_LINES; i < (set * SET_LINES) + (SET_LINES - 1); i++) {
                if (cache[i].isValid()) {
                    if (pos >= cache[i].getMemoryStart() && pos <= cache[i].getMemoryEnd()) {
                        time += 0.01;
                        ram[pos] = data;
                        cache[i].setModified(true);
                        return;
                    }
                }
            }
            
            if (cache[setAssociativePointers[set] + (set * SET_LINES)].isValid()) {
                if (cache[setAssociativePointers[set] + (set * SET_LINES)].isModified()) {
                    time += 0.66 + 0.66 + 0.01;
                } else {
                    time += 0.66 + 0.01;
                }
            } else {
                time += 0.66 + 0.01;
            }

            cache[setAssociativePointers[set] + (set * SET_LINES)].setMemoryStart((pos / BLOCK_LENGTH) * BLOCK_LENGTH);
            cache[setAssociativePointers[set] + (set * SET_LINES)].setMemoryEnd(cache[setAssociativePointers[set] + (set * SET_LINES)].getMemoryStart() + BLOCK_LENGTH - 1);
            cache[setAssociativePointers[set] + (set * SET_LINES)].setModified(true);
            setAssociativePointers[set]++;
            if (setAssociativePointers[set] >= SET_LINES) {
                setAssociativePointers[set] = 0;
            }

            ram[pos] = data;
        }
    }

    public static void test(byte t) {
        byte type = t;
        write(type, 100, 10);
        write(type, 101, 13);
        write(type, 102, 21);
        write(type, 103, 11);
        write(type, 104, 67);
        write(type, 105, 43);
        write(type, 106, 9);
        write(type, 107, 11);
        write(type, 108, 19);
        write(type, 109, 23);
        write(type, 110, 32);
        write(type, 111, 54);
        write(type, 112, 98);
        write(type, 113, 7);
        write(type, 114, 13);
        write(type, 115, 1);

        int menor = read(type, 100);
        int mayor = menor;
        int k = 0;
        for (int i = 101; i <= 115; i++) {
            k++;
            write(type, 615, k);
            if (read(type, i) < menor) {
                menor = read(type, i);
            }
            if (read(type, i) > mayor) {
                mayor = read(type, i);
            }
        }
    }
}
