/*
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
 */
package org.codice.imaging.nitf.core.image;

import org.codice.imaging.nitf.core.common.AbstractSegmentParser;
import org.codice.imaging.nitf.core.common.FileType;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.common.ParseStrategy;
import org.codice.imaging.nitf.core.common.NitfReader;
import static org.codice.imaging.nitf.core.image.ImageConstants.ABPP_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.COMRAT_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.IALVL_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.ICAT_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.ICOM_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.ICORDS_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.IC_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.IDLVL_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.IGEOLO_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.IID1_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.IID2_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.ILOC_HALF_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.IM;
import static org.codice.imaging.nitf.core.image.ImageConstants.IMAG_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.IMODE_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.IREP_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.ISORCE_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.ISYNC_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.IXSHDL_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.IXSOFL_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.NBANDS_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.NBPC_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.NBPP_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.NBPR_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.NCOLS_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.NICOM_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.NPPBH_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.NPPBV_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.NROWS_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.PJUST_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.PVTYPE_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.TGTID_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.UDIDL_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.UDOFL_LENGTH;
import static org.codice.imaging.nitf.core.image.ImageConstants.XBANDS_LENGTH;
import org.codice.imaging.nitf.core.security.SecurityMetadataParser;
import org.codice.imaging.nitf.core.tre.TreCollection;
import org.codice.imaging.nitf.core.tre.TreSource;

/**
    Parser for an image segment in a NITF file.
*/
public class ImageSegmentParser extends AbstractSegmentParser {

    private int numImageComments = 0;
    private int numBands = 0;
    private int userDefinedImageDataLength = 0;
    private int imageExtendedSubheaderDataLength = 0;

    private ImageSegmentImpl segment = null;

    /**
     * Default constructor.
     */
    public ImageSegmentParser() {
    }

    /**
     * Parse the image segment header
     * <p>
     * This will return the image segment, but it is not threadsafe. Please create a new parser for each segment, or
     * protect against parallel runs.
     * @param nitfReader the reader to use to get the data
     * @param parseStrategy the parsing strategy to use to process the data
     * @param dataLength the length of the data associated with this segment.
     * @return the parsed image segment
     * @throws NitfFormatException on parse failure
     */
    public final ImageSegmentImpl parse(final NitfReader nitfReader, final ParseStrategy parseStrategy,
            final long dataLength) throws NitfFormatException {
        reader = nitfReader;
        segment = new ImageSegmentImpl();
        segment.setDataLength(dataLength);
        parsingStrategy = parseStrategy;

        readIM();
        readIID1();
        readIDATIM();
        readTGTID();
        readIID2();
        segment.setSecurityMetadata(new SecurityMetadataParser().parseSecurityMetadata(reader));
        readENCRYP();
        readISORCE();
        readNROWS();
        readNCOLS();
        readPVTYPE();
        readIREP();
        readICAT();
        readABPP();
        readPJUST();
        readICORDS();
        if ((segment.getImageCoordinatesRepresentation() != ImageCoordinatesRepresentation.UNKNOWN)
            && (segment.getImageCoordinatesRepresentation() != ImageCoordinatesRepresentation.NONE)) {
            readIGEOLO();
        }
        readNICOM();
        for (int i = 0; i < numImageComments; ++i) {
            segment.addImageComment(reader.readTrimmedBytes(ICOM_LENGTH));
        }
        readIC();
        if (segment.hasCOMRAT()) {
            readCOMRAT();
        }
        readNBANDS();
        if ((reader.getFileType() != FileType.NITF_TWO_ZERO) && (numBands == 0)) {
            readXBANDS();
        }
        for (int i = 0; i < numBands; ++i) {
            ImageBand imageBand = new ImageBand();
            new ImageBandParser(reader, imageBand);
            segment.addImageBand(imageBand);
        }
        readISYNC();
        readIMODE();
        readNBPR();
        readNBPC();
        readNPPBH();
        readNPPBV();
        readNBPP();
        readIDLVL();
        readIALVL();
        readILOC();
        readIMAG();
        readUDIDL();
        if (userDefinedImageDataLength > 0) {
            readUDOFL();
            readUDID();
        }
        readIXSHDL();
        if (imageExtendedSubheaderDataLength > 0) {
            readIXSOFL();
            readIXSHD();
        }
        return segment;
    }

    private void readIM() throws NitfFormatException {
       reader.verifyHeaderMagic(IM);
    }

    private void readIID1() throws NitfFormatException {
        segment.setIdentifier(reader.readTrimmedBytes(IID1_LENGTH));
    }

    private void readIDATIM() throws NitfFormatException {
        segment.setImageDateTime(readNitfDateTime());
    }

    private void readTGTID() throws NitfFormatException {
        segment.setImageTargetId(new TargetId(reader.readBytes(TGTID_LENGTH)));
    }

    private void readIID2() throws NitfFormatException {
        segment.setImageIdentifier2(reader.readTrimmedBytes(IID2_LENGTH));
    }

    private void readISORCE() throws NitfFormatException {
        segment.setImageSource(reader.readTrimmedBytes(ISORCE_LENGTH));
    }

    private void readNROWS() throws NitfFormatException {
        segment.setNumberOfRows(reader.readBytesAsLong(NROWS_LENGTH));
    }

    private void readNCOLS() throws NitfFormatException {
        segment.setNumberOfColumns(reader.readBytesAsLong(NCOLS_LENGTH));
    }

    private void readPVTYPE() throws NitfFormatException {
        String pvtype = reader.readTrimmedBytes(PVTYPE_LENGTH);
        segment.setPixelValueType(PixelValueType.getEnumValue(pvtype));
    }

    private void readIREP() throws NitfFormatException {
        String irep = reader.readTrimmedBytes(IREP_LENGTH);
        segment.setImageRepresentation(ImageRepresentation.getEnumValue(irep));
    }

    private void readICAT() throws NitfFormatException {
        String icat = reader.readTrimmedBytes(ICAT_LENGTH);
        segment.setImageCategory(ImageCategory.getEnumValue(icat));
    }

    private void readABPP() throws NitfFormatException {
        segment.setActualBitsPerPixelPerBand(reader.readBytesAsInteger(ABPP_LENGTH));
    }

    private void readPJUST() throws NitfFormatException {
        String pjust = reader.readTrimmedBytes(PJUST_LENGTH);
        segment.setPixelJustification(PixelJustification.getEnumValue(pjust));
    }

    private void readICORDS() throws NitfFormatException {
        String icords = reader.readBytes(ICORDS_LENGTH);
        segment.setImageCoordinatesRepresentation(ImageCoordinatesRepresentation.getEnumValue(icords, reader.getFileType()));
    }

    private void readIGEOLO() throws NitfFormatException {
        // TODO: this really only handle the GEO and D cases, not the UTM / UPS representations.
        final int numCoordinates = 4;
        final int coordinatePairLength = IGEOLO_LENGTH / numCoordinates;
        String igeolo = reader.readBytes(IGEOLO_LENGTH);
        ImageCoordinatePair[] coords = new ImageCoordinatePair[numCoordinates];
        for (int i = 0; i < numCoordinates; ++i) {
            coords[i] = new ImageCoordinatePair();
            String coordStr = igeolo.substring(i * coordinatePairLength, (i + 1) * coordinatePairLength);
            switch (segment.getImageCoordinatesRepresentation()) {
                case GEOGRAPHIC:
                    coords[i].setFromDMS(coordStr);
                    break;
                case DECIMALDEGREES:
                    coords[i].setFromDecimalDegrees(coordStr);
                    break;
                case UTMUPSNORTH:
                    coords[i].setFromUTMUPSNorth(coordStr);
                    break;
                case GEOCENTRIC:
                    coords[i].setFromDMS(coordStr);
                    break;
                case MGRS:
                    coords[i].setFromMGRS(coordStr);
                    break;
                default:
                    throw new UnsupportedOperationException("NEED TO IMPLEMENT OTHER COORDINATE REPRESENTATIONS: "
                                                            + segment.getImageCoordinatesRepresentation());
            }
        }
        segment.setImageCoordinates(new ImageCoordinates(coords));
    }

    private void readNICOM() throws NitfFormatException {
        numImageComments = reader.readBytesAsInteger(NICOM_LENGTH);
    }

    private void readIC() throws NitfFormatException {
        String ic = reader.readBytes(IC_LENGTH);
        segment.setImageCompression(ImageCompression.getEnumValue(ic));
    }

    private void readNBANDS() throws NitfFormatException {
        numBands = reader.readBytesAsInteger(NBANDS_LENGTH);
    }

    private void readXBANDS() throws NitfFormatException {
        numBands = reader.readBytesAsInteger(XBANDS_LENGTH);
    }

    private void readISYNC() throws NitfFormatException {
        reader.readBytes(ISYNC_LENGTH);
    }

    private void readIMODE() throws NitfFormatException {
        String imode = reader.readBytes(IMODE_LENGTH);
        segment.setImageMode(ImageMode.getEnumValue(imode));
    }

    private void readNBPR() throws NitfFormatException {
        segment.setNumberOfBlocksPerRow(reader.readBytesAsInteger(NBPR_LENGTH));
    }

    private void readNBPC() throws NitfFormatException {
        segment.setNumberOfBlocksPerColumn(reader.readBytesAsInteger(NBPC_LENGTH));
    }

    private void readNPPBH() throws NitfFormatException {
        segment.setNumberOfPixelsPerBlockHorizontalRaw(reader.readBytesAsInteger(NPPBH_LENGTH));
    }

    private void readNPPBV() throws NitfFormatException {
        segment.setNumberOfPixelsPerBlockVerticalRaw(reader.readBytesAsInteger(NPPBV_LENGTH));
    }

    private void readNBPP() throws NitfFormatException {
        segment.setNumberOfBitsPerPixelPerBand(reader.readBytesAsInteger(NBPP_LENGTH));
    }

    private void readIDLVL() throws NitfFormatException {
        segment.setImageDisplayLevel(reader.readBytesAsInteger(IDLVL_LENGTH));
    }

    private void readIALVL() throws NitfFormatException {
        segment.setAttachmentLevel(reader.readBytesAsInteger(IALVL_LENGTH));
    }

    private void readILOC() throws NitfFormatException {
        segment.setImageLocationRow(reader.readBytesAsInteger(ILOC_HALF_LENGTH));
        segment.setImageLocationColumn(reader.readBytesAsInteger(ILOC_HALF_LENGTH));
    }

    private void readIMAG() throws NitfFormatException {
        segment.setImageMagnification(reader.readBytes(IMAG_LENGTH));
    }

    private void readUDIDL() throws NitfFormatException {
        userDefinedImageDataLength = reader.readBytesAsInteger(UDIDL_LENGTH);
    }

    private void readUDOFL() throws NitfFormatException {
        segment.setUserDefinedHeaderOverflow(reader.readBytesAsInteger(UDOFL_LENGTH));
    }

    private void readUDID() throws NitfFormatException {
        TreCollection userDefinedSubheaderTres = parsingStrategy.parseTREs(reader,
                userDefinedImageDataLength - UDOFL_LENGTH,
                TreSource.UserDefinedImageData);
        segment.mergeTREs(userDefinedSubheaderTres);
    }

    private void readIXSHDL() throws NitfFormatException {
        imageExtendedSubheaderDataLength = reader.readBytesAsInteger(IXSHDL_LENGTH);
    }

    private void readIXSOFL() throws NitfFormatException {
        segment.setExtendedHeaderDataOverflow(reader.readBytesAsInteger(IXSOFL_LENGTH));
    }

    private void readIXSHD() throws NitfFormatException {
        TreCollection extendedSubheaderTres = parsingStrategy.parseTREs(reader,
                imageExtendedSubheaderDataLength - IXSOFL_LENGTH,
                TreSource.ImageExtendedSubheaderData);
        segment.mergeTREs(extendedSubheaderTres);
    }

    private void readCOMRAT() throws NitfFormatException {
        segment.setCompressionRate(reader.readTrimmedBytes(COMRAT_LENGTH));
    }
}
