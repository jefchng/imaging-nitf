/**
 * Copyright (c) Codice Foundation
 * 
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 * 
 **/
package org.codice.nitf.filereader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NitfHeaderReader
{
    private Boolean hasNitfHeader = false;
    private NitfVersion nitfVersion = NitfVersion.UNKNOWN;
    private int nitfComplexityLevel = 0;
    private String nitfStandardType = null;
    private String nitfOriginatingStationId = null;
    private Date nitfFileDateTime = null;
    private String nitfFileTitle = null;
    private NitfSecurityClassification nitfSecurityClassification = NitfSecurityClassification.UNKNOWN;
    private String nitfFileSecurityClassificationSystem = null;
    private String nitfFileCodewords = null;
    private String nitfFileControlAndHandling = null;
    private String nitfFileReleaseInstructions = null;
    // Could be an enumerated type
    private String nitfFileDeclassificationType = null;
    // String instead of Date because its frequently just an empty string
    private String nitfFileDeclassificationDate = null;
    private String nitfFileDeclassificationExemption = null;
    private String nitfFileDowngrade = null;
    // String instead of Date because its frequently just an empty string
    private String nitfFileDowngradeDate = null;
    private String nitfFileClassificationText = null;
    // Could be an enumerated type
    private String nitfFileClassificationAuthorityType = null;
    private String nitfFileClassificationAuthority = null;
    private String nitfFileClassificationReason = null;
    private String nitfFileSecuritySourceDate = null;
    private String nitfFileSecurityControlNumber = null;
    private String nitfFileCopyNumber = null;
    private String nitfFileNumberOfCopies = null;
    private int nitfFileBackgroundColourRed = 0;
    private int nitfFileBackgroundColourGreen = 0;
    private int nitfFileBackgroundColourBlue = 0;
    private String nitfOriginatorsName = null;
    private String nitfOriginatorsPhoneNumber = null;
    private long nitfFileLength = -1;
    private int nitfHeaderLength = -1;

    private BufferedReader reader;
    private int numBytesRead = 0;

    private static final int FHDR_LENGTH = 4;
    private static final String NITF_FHDR = "NITF";
    private static final int FVER_LENGTH = 5;
    private static final int CLEVEL_LENGTH = 2;
    private static final int STYPE_LENGTH = 4;
    private static final int OSTAID_LENGTH = 10;
    private static final int FDT_LENGTH = 14;
    private static final int FTITLE_LENGTH = 80;
    private static final int FSCLAS_LENGTH = 1;
    private static final int FSCLSY_LENGTH = 2;
    private static final int FSCODE_LENGTH = 11;
    private static final int FSCTLH_LENGTH = 2;
    private static final int FSREL_LENGTH = 20;
    private static final int FSDCTP_LENGTH = 2;
    private static final int FSDCDT_LENGTH = 8;
    private static final int FSDCXM_LENGTH = 4;
    private static final int FSDG_LENGTH = 1;
    private static final int FSDGDT_LENGTH = 8;
    private static final int FSCLTX_LENGTH = 43;
    private static final int FSCATP_LENGTH = 1;
    private static final int FSCAUT_LENGTH = 40;
    private static final int FSCRSN_LENGTH = 1;
    private static final int FSSRDT_LENGTH = 8;
    private static final int FSCTLN_LENGTH = 15;
    private static final int FSCOP_LENGTH = 5;
    private static final int FSCPYS_LENGTH = 5;
    private static final int ENCRYP_LENGTH = 1;
    private static final int FBKGC_LENGTH = 3;
    private static final int ONAME_LENGTH = 24;
    private static final int OPHONE_LENGTH = 18;
    private static final int FL_LENGTH = 12;
    private static final int HL_LENGTH = 6;

    public NitfHeaderReader(InputStream nitfInputStream) throws ParseException {
        reader = new BufferedReader(new InputStreamReader(nitfInputStream));
        readBaseHeader();
    }

    public Boolean isNitf() {
        return hasNitfHeader;
    }

    public NitfVersion getVersion() {
        return nitfVersion;
    }

    public int getComplexityLevel() {
        return nitfComplexityLevel;
    }

    public String getStandardType() {
        return nitfStandardType;
    }

    public String getOriginatingStationId() {
        return nitfOriginatingStationId;
    }

    public Date getFileDateTime() {
        return nitfFileDateTime;
    }

    public String getFileTitle() {
        return nitfFileTitle;
    }

    public NitfSecurityClassification getSecurityClassification() {
        return nitfSecurityClassification;
    }

    public String getFileSecurityClassificationSystem() {
        return nitfFileSecurityClassificationSystem;
    }

    public String getFileCodewords() {
        return nitfFileCodewords;
    }

    public String getFileControlAndHandling() {
        return nitfFileControlAndHandling;
    }

    public String getFileReleaseInstructions() {
        return nitfFileReleaseInstructions;
    }

    public String getFileDeclassificationType() {
        return nitfFileDeclassificationType;
    }

    public String getFileDeclassificationDate() {
        return nitfFileDeclassificationDate;
    }

    public String getFileDeclassificationExemption() {
        return nitfFileDeclassificationExemption;
    }

    public String getFileDowngrade() {
        return nitfFileDowngrade;
    }

    public String getFileDowngradeDate() {
        return nitfFileDowngradeDate;
    }

    public String getFileClassificationText() {
        return nitfFileClassificationText;
    }

    public String getFileClassificationAuthorityType() {
        return nitfFileClassificationAuthorityType;
    }

    public String getFileClassificationAuthority() {
        return nitfFileClassificationAuthority;
    }

    public String getFileClassificationReason() {
        return nitfFileClassificationReason;
    }

    public String getFileSecuritySourceDate() {
        return nitfFileSecuritySourceDate;
    }

    public String getFileSecurityControlNumber() {
        return nitfFileSecurityControlNumber;
    }

    public String getFileCopyNumber() {
        return nitfFileCopyNumber;
    }

    public String getFileNumberOfCopies() {
        return nitfFileNumberOfCopies;
    }

    public int getFileBackgroundColourRed() {
        return nitfFileBackgroundColourRed;
    }

    public int getFileBackgroundColourGreen() {
        return nitfFileBackgroundColourGreen;
    }

    public int getFileBackgroundColourBlue() {
        return nitfFileBackgroundColourBlue;
    }

    public String getOriginatorsName() {
        return nitfOriginatorsName;
    }

    public String getOriginatorsPhoneNumber() {
        return nitfOriginatorsPhoneNumber;
    }

    public long getFileLength() {
        return nitfFileLength;
    }

    public int getHeaderLength() {
        return nitfHeaderLength;
    }

    private void readBaseHeader() throws ParseException {
        readFHDR();
        readFVER();
        readCLEVEL();
        readSTYPE();
        readOSTAID();
        readFDT();
        readFTITLE();
        readFSCLAS();
        readFSCLSY();
        readFSCODE();
        readFSCTLH();
        readFSREL();
        readFSDCTP();
        readFSDCDT();
        readFSDCXM();
        readFSDG();
        readFSDGDT();
        readFSCLTX();
        readFSCATP();
        readFSCAUT();
        readFSCRSN();
        readFSSRDT();
        readFSCTLN();
        readFSCOP();
        readFSCPYS();
        readENCRYP();
        readFBKGC();
        readONAME();
        readOPHONE();
        readFL();
        readHL();
    }

    private void readFHDR() throws ParseException {
        String fhdr = readBytes(FHDR_LENGTH);
        hasNitfHeader = fhdr.equals(NITF_FHDR);
    }

    private void readFVER() throws ParseException {
        String fver = readBytes(FVER_LENGTH);
        for (NitfVersion version : NitfVersion.values()) {
            if (fver.equals(version.getTextEquivalent())) {
                nitfVersion = version;
            }
        }
    }

    private void readCLEVEL() throws ParseException {
        String clevel = readBytes(CLEVEL_LENGTH);
        try {
            nitfComplexityLevel = Integer.parseInt(clevel);
            if ((nitfComplexityLevel < 0) || (nitfComplexityLevel > 99)) {
                new ParseException(String.format("CLEVEL out of range: %i", nitfComplexityLevel), numBytesRead);
            }
        } catch (NumberFormatException ex) {
            new ParseException(String.format("Bad CLEVEL format: %s", clevel), numBytesRead);
        }
    }

    private void readSTYPE() throws ParseException {
        nitfStandardType = readBytes(STYPE_LENGTH);
    }

    private void readOSTAID() throws ParseException {
        nitfOriginatingStationId = readTrimmedBytes(OSTAID_LENGTH);
    }

    private void readFDT() throws ParseException {
        String fdt = readBytes(FDT_LENGTH);
        // TODO: check if NITF 2.0 uses the same format.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        nitfFileDateTime = dateFormat.parse(fdt);
        if (nitfFileDateTime == null) {
            new ParseException(String.format("Bad FDT format: %s", fdt), numBytesRead);
        }
    }

    private void readFTITLE() throws ParseException {
        nitfFileTitle = readTrimmedBytes(FTITLE_LENGTH);
    }

    private void readFSCLAS() throws ParseException {
        String fsclas = readBytes(FSCLAS_LENGTH);
        for (NitfSecurityClassification classification : NitfSecurityClassification.values()) {
            if (fsclas.equals(classification.getTextEquivalent())) {
                nitfSecurityClassification = classification;
            }
        }
    }

    private void readFSCLSY() throws ParseException {
        nitfFileSecurityClassificationSystem = readTrimmedBytes(FSCLSY_LENGTH);
    }

    private void readFSCODE() throws ParseException {
        nitfFileCodewords = readTrimmedBytes(FSCODE_LENGTH);
    }

    private void readFSCTLH() throws ParseException {
        nitfFileControlAndHandling = readTrimmedBytes(FSCTLH_LENGTH);
    }

    private void readFSREL() throws ParseException {
        nitfFileReleaseInstructions = readTrimmedBytes(FSREL_LENGTH);
    }

    private void readFSDCTP() throws ParseException {
        nitfFileDeclassificationType = readTrimmedBytes(FSDCTP_LENGTH);
    }

    private void readFSDCDT() throws ParseException {
        nitfFileDeclassificationDate = readTrimmedBytes(FSDCDT_LENGTH);
    }

    private void readFSDCXM() throws ParseException {
        nitfFileDeclassificationExemption = readTrimmedBytes(FSDCXM_LENGTH);
    }

    private void readFSDG() throws ParseException {
        nitfFileDowngrade = readTrimmedBytes(FSDG_LENGTH);
    }

    private void readFSDGDT() throws ParseException {
        nitfFileDowngradeDate = readTrimmedBytes(FSDGDT_LENGTH);
    }

    private void readFSCLTX() throws ParseException {
        nitfFileClassificationText= readTrimmedBytes(FSCLTX_LENGTH);
    }

    private void readFSCATP() throws ParseException {
        nitfFileClassificationAuthorityType = readTrimmedBytes(FSCATP_LENGTH);
    }

    private void readFSCAUT() throws ParseException {
        nitfFileClassificationAuthority = readTrimmedBytes(FSCAUT_LENGTH);
    }

    private void readFSCRSN() throws ParseException {
        nitfFileClassificationReason = readTrimmedBytes(FSCRSN_LENGTH);
    }

    private void readFSSRDT() throws ParseException {
        nitfFileSecuritySourceDate = readTrimmedBytes(FSSRDT_LENGTH);
    }

    private void readFSCTLN() throws ParseException {
        nitfFileSecurityControlNumber = readTrimmedBytes(FSCTLN_LENGTH);
    }

    private void readFSCOP() throws ParseException {
        nitfFileCopyNumber = readTrimmedBytes(FSCOP_LENGTH);
    }

    private void readFSCPYS() throws ParseException {
        nitfFileNumberOfCopies = readTrimmedBytes(FSCPYS_LENGTH);
    }

    private void readENCRYP() throws ParseException {
        if (!readBytes(ENCRYP_LENGTH).equals("0")) {
            new ParseException("Unexpected ENCRYP values", numBytesRead);
        }
    }

    private void readFBKGC() throws ParseException {
        String fbkgc = readBytes(FBKGC_LENGTH);
        try {
            nitfFileBackgroundColourRed = Integer.parseInt(fbkgc.substring(0, 1));
            nitfFileBackgroundColourGreen = Integer.parseInt(fbkgc.substring(1, 2));
            nitfFileBackgroundColourBlue = Integer.parseInt(fbkgc.substring(2));
        } catch (NumberFormatException ex) {
            new ParseException("Bad FBKGC", numBytesRead);
        }
    }

    private void readONAME() throws ParseException {
        nitfOriginatorsName = readTrimmedBytes(ONAME_LENGTH);
    }

    private void readOPHONE() throws ParseException {
        nitfOriginatorsPhoneNumber = readTrimmedBytes(OPHONE_LENGTH);
    }

    private void readFL() throws ParseException {
        String fl = readBytes(FL_LENGTH);
        try {
            nitfFileLength = Long.parseLong(fl);
        } catch (NumberFormatException ex) {
            new ParseException("Bad FL", numBytesRead);
        }
    }

    private void readHL() throws ParseException {
        String hl = readBytes(HL_LENGTH);
        try {
            nitfHeaderLength = Integer.parseInt(hl);
        } catch (NumberFormatException ex) {
            new ParseException("Bad HL", numBytesRead);
        }
    }

    private String readTrimmedBytes(int count) throws ParseException {
        return readBytes(count).trim();
    }

    private String readBytes(int count) throws ParseException {
        int thisRead = 0;
        try {
            char[] bytes = new char[count];
            thisRead = reader.read(bytes, 0, count);
            if (thisRead == -1) {
                new ParseException("End of file reading from NITF stream.", numBytesRead);
            } else if (thisRead < count) {
                new ParseException("Short read while reading from NITF stream.", numBytesRead + thisRead);
            }
            numBytesRead += thisRead;
            return String.valueOf(bytes);
        } catch (IOException ex) {
            new ParseException("Error reading from NITF stream: " + ex.getMessage(), numBytesRead);
        }
        return null;
    }

}
