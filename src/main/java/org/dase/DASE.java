package org.dase;

import java.io.File;

public class DASE {


    public static void main(String[] args) {
        File orkFile = new File("rockets/superbigbertha.ork");
        OpenRocket sim = new OpenRocket(orkFile);

        sim.setSimulation();
        // DASE stuff


        sim.runSimulation();
    }



}
