package org.dase;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DASE {

    public static void main(String[] args) {

        // Excel stuff
        XSSFWorkbook workbook_ell4;

        try{
            FileInputStream ell4 = new FileInputStream("cur/CUR_Ell4_in.xlsx");
            workbook_ell4 = new XSSFWorkbook(ell4);
        }catch(Exception e){
            System.err.println("Failed to load CUR file: " + e.getMessage());
            return;
        }

        XSSFSheet sheet_ell4 = workbook_ell4.getSheetAt(0);

        // Open Rocket Stuff
        File orkFile = new File("rockets/superbigbertha.ork");
        OpenRocket sim = new OpenRocket(orkFile);
        sim.setSimulation();

        // DASE stuff

        // set the categorical factors
        sim.setNumFins(3); // set the number of fins
        sim.setRocketConeShape("ellipsoid"); // set the cone shape

        double CM_TO_METERS = (double) 1 / 100; // OpenRocket deals in meters.

        for (Row row : sheet_ell4) {
            // Get the row data from the CUR in file
            if (row.getCell(0).getCellType() == CellType.STRING)
            {
                continue; // This is the column header. Not parseable for us here
            }
            int treatment = (int) row.getCell(0).getNumericCellValue(); // Tmt#
            int mc = (int) row.getCell(1).getNumericCellValue(); // MC#
            double L_tube = row.getCell(2).getNumericCellValue() * CM_TO_METERS; // tube length
            double L_cone = row.getCell(3).getNumericCellValue() * CM_TO_METERS; // cone length
            double d_f = row.getCell(4).getNumericCellValue() * CM_TO_METERS; // fin displacement
            double v_wnd = row.getCell(5).getNumericCellValue(); // wind velocity
            double angle_rod = row.getCell(6).getNumericCellValue(); // rod angle (into wind)

            // Give the rocket a little treatment
            sim.setRocketTubeLength(L_tube);
            sim.setRocketConeLength(L_cone);
            sim.setRocketFinRelativePlacement(d_f);
            sim.setWindVelocity(v_wnd);
            sim.setRodAngle(angle_rod);

            // Run it!
            sim.runSimulation();

            // Get the apogee!
            System.out.println("Apogee: " + sim.getApogee());

            // Spit out the data (CUR OUT)

        }

    }

}
