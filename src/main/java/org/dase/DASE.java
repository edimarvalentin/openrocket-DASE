package org.dase;

import java.io.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DASE {

    public static void main(String[] args) {

        // Excel stuff
        XSSFWorkbook workbook_in;
        XSSFWorkbook workbook_out;

        try{
            FileInputStream CUR_IN = new FileInputStream("cur/CUR_Ell4_in.xlsx");
            workbook_in = new XSSFWorkbook(CUR_IN);
        }catch(Exception e){
            System.err.println("Failed to load CUR_in file: " + e.getMessage());
            return;
        }


        workbook_out = new XSSFWorkbook();
        workbook_out.createSheet("Sheet1");


        XSSFSheet sheet_in = workbook_in.getSheetAt(0);
        XSSFSheet sheet_out = workbook_out.getSheetAt(0);

        // Open Rocket Stuff
        File orkFile = new File("rockets/superbigbertha.ork");
        OpenRocket sim = new OpenRocket(orkFile);
        sim.setSimulation();

        // DASE stuff

        // set the categorical factors
        sim.setNumFins(3); // set the number of fins
        sim.setRocketConeShape("ellipsoid"); // set the cone shape

        double CM_TO_METERS = (double) 1 / 100; // OpenRocket deals in meters.

        int rownum = 0;
        for (Row row : sheet_in) {
            // Get the row data from the CUR in file
            if (row.getCell(0).getCellType() == CellType.STRING)
            {
                Row row_out = sheet_out.createRow(rownum);

                Cell cell = row_out.createCell(0);
                cell.setCellValue("Tmt#");

                cell = row_out.createCell(1);
                cell.setCellValue("MC#");

                cell = row_out.createCell(2);
                cell.setCellValue("L_{tube}");

                cell = row_out.createCell(3);
                cell.setCellValue("L_{cone}");

                cell = row_out.createCell(4);
                cell.setCellValue("d_{f}");

                cell = row_out.createCell(5);
                cell.setCellValue("WS");

                cell = row_out.createCell(6);
                cell.setCellValue("theta_{rod}");

                cell = row_out.createCell(7);
                cell.setCellValue("h_{max}");

                cell = row_out.createCell(8);
                cell.setCellValue("P_{succ}");

                rownum++;

                continue; // This is the column header. Not parseable for us here
            }
            int treatment       = (int) row.getCell(0).getNumericCellValue();           // Tmt#
            int mc              = (int) row.getCell(1).getNumericCellValue();           // MC#
            double L_tube       = row.getCell(2).getNumericCellValue() * CM_TO_METERS;  // tube length
            double L_cone       = row.getCell(3).getNumericCellValue() * CM_TO_METERS;  // cone length
            double d_f          = row.getCell(4).getNumericCellValue() * CM_TO_METERS;  // fin displacement
            double v_wnd        = row.getCell(5).getNumericCellValue();                 // wind velocity
            double angle_rod    = row.getCell(6).getNumericCellValue();                 // rod angle (into wind)

            // Give the rocket a little treatment
            sim.setRocketTube1Length(L_tube / 2);
            sim.setRocketTube2Length(L_tube / 2);
            sim.setRocketConeLength(L_cone);
            sim.setRocketFinRelativePlacement(d_f);
            sim.setWindVelocity(v_wnd);
            sim.setRodAngle(angle_rod);

            // Run it!
            sim.runSimulation();

            // Get the apogee!
            double apogee = sim.getApogee();
            System.out.println("Apogee: " + apogee);

            Row row_out = sheet_out.createRow(rownum);

            Cell cell = row_out.createCell(0);
            cell.setCellValue(treatment);

            cell = row_out.createCell(1);
            cell.setCellValue(mc);

            cell = row_out.createCell(2);
            cell.setCellValue(L_tube);

            cell = row_out.createCell(3);
            cell.setCellValue(L_cone);

            cell = row_out.createCell(4);
            cell.setCellValue(d_f);

            cell = row_out.createCell(5);
            cell.setCellValue(v_wnd);

            cell = row_out.createCell(6);
            cell.setCellValue(angle_rod);

            cell = row_out.createCell(7);
            cell.setCellValue(apogee);

            cell = row_out.createCell(8);
            cell.setCellValue(0);

            // Spit out the data (CUR OUT)
            File outDir = new File("out/tmt"+treatment+"_mc"+mc +"/superbigbertha.ork");
            if(outDir.mkdirs()){
                sim.saveRocket(outDir);

                try{
                    FileWriter writer = new FileWriter("out/tmt"+treatment+"_mc"+mc +"/simconditions.txt");
                    writer.write(sim.getFlightConfiguration() + "\n");
                    writer.append(sim.getSimConditions()).append("\n");
                    writer.close();
                }catch (Exception e){
                    System.err.println("Failed to write simconditions.txt file: " + e.getMessage());
                }
            }
            rownum++;
        }

        try{
            FileOutputStream out = new FileOutputStream("out/CUR_out.xlsx");
            workbook_out.write(out);
        }catch (Exception e){
            System.err.println("Failed to write CUR_out file: " + e.getMessage());
        }

    }

}
