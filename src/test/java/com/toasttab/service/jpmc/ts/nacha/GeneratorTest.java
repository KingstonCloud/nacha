package com.toasttab.service.jpmc.ts.nacha;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.afrunt.jach.ACH;
import com.afrunt.jach.document.ACHBatch;
import com.afrunt.jach.document.ACHBatchDetail;
import com.afrunt.jach.document.ACHDocument;
import com.afrunt.jach.domain.AddendaRecord;
import com.afrunt.jach.domain.BatchControl;
import com.afrunt.jach.domain.FileControl;
import com.afrunt.jach.domain.FileHeader;
import com.afrunt.jach.domain.GeneralBatchHeader;
import com.afrunt.jach.domain.addenda.GeneralAddendaRecord;
import com.afrunt.jach.domain.detail.CCDEntryDetail;
import com.afrunt.jach.logic.ACHWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class GeneratorTest {

    @Test
    public void testGenerator() throws FileNotFoundException {

        Date aDate = Date.from(LocalDateTime.parse("2019-07-09T06:30:00").toInstant(ZoneOffset.UTC));

        //////////////////////////////
        // File Header Record
        //////////////////////////////

        FileHeader header = new FileHeader();

        assertEquals(header.getRecordTypeCode(), "1");                   // 1
        header.setPriorityCode("01");                                           // 2
        header.setImmediateDestination("b021000021");                           // 3
        header.setImmediateOrigin("0000000000");                                // 4
        header.setFileCreationDate(aDate);                                      // 5
        header.setFileCreationTime("0630");                                     // 6
        header.setFileIdModifier("X");                                          // 7
        assertEquals(header.getRecordSize(), "094");                     // 8
        header.setBlockingFactor("10");                                         // 9
        header.setFormatCode("1");                                              // 10
        header.setImmediateDestinationName("JPMORGAN CHASE");                   // 11
        header.setImmediateOriginName("TOAST");                                 // 12
        assertNull(header.getReferenceCode());                                  // 13

        //////////////////////////////
        // Batch Header Record
        //////////////////////////////

        GeneralBatchHeader batchHeader = new GeneralBatchHeader();

        assertEquals(batchHeader.getRecordTypeCode(), "5");             // 1
        batchHeader.setServiceClassCode("220");                                // 2
        batchHeader.setCompanyName("TOAST");                                   // 3
        batchHeader.setCompanyDiscretionaryData("12345678");                   // 4
        batchHeader.setCompanyID("0000000000");                                // 5
        batchHeader.setStandardEntryClassCode("CCD");                          // 6
        batchHeader.setCompanyEntryDescription("CCD");                         // 7
        batchHeader.setCompanyDescriptiveDate("JAN 03");                       // 8
        batchHeader.setEffectiveEntryDate(aDate);                              // 9
        assertNull(batchHeader.getSettlementDate());                           // 10
        batchHeader.setOriginatorStatusCode("1");                              // 11
        batchHeader.setOriginatorDFIIdentifier("02100002");                    // 12
        batchHeader.setBatchNumber(1);                                         // 13

        //////////////////////////////
        // Entry Detail Record
        //////////////////////////////
        CCDEntryDetail entryDetail = new CCDEntryDetail();

        assertEquals("6", entryDetail.getRecordTypeCode());           // 1
        entryDetail.setTransactionCode(27);                                    // 2
        entryDetail.setReceivingDfiIdentification("12345678");                 // 3
        entryDetail.setCheckDigit(new Short("1"));                          // 4
        entryDetail.setDfiAccountNumber("DFIACCOUNTNUMBER");                   // 5
        entryDetail.setAmount(new BigDecimal("123.45"));                   // 6
        entryDetail.setIdentificationNumber("INDIVIDNUMBER");                   // 7
        entryDetail.setReceivingCompanyName("SIMISANDWICHSHOP");               // 8
        assertNull(entryDetail.getDiscretionaryData());                         // 9
        entryDetail.setAddendaRecordIndicator(new Short("1"));              // 10
        entryDetail.setTraceNumber(123456789l);                                 // 11

        //////////////////////////////
        // Addenda Record
        //////////////////////////////
        GeneralAddendaRecord addendaRecord = new GeneralAddendaRecord();
        assertEquals("7", addendaRecord.getRecordTypeCode());       // 1
        assertEquals("05", addendaRecord.getAddendaTypeCode());     // 2
        addendaRecord.setPaymentRelatedInformation("Payment related info."); // 3
        addendaRecord.setAddendaSequenceNumber(0001);                        // 4
        addendaRecord.setEntryDetailSequenceNumber(123l);                    // 5

        //////////////////////////////
        // Batch Control Record
        //////////////////////////////
        BatchControl batchControl = new BatchControl();
        assertEquals("8", batchControl.getRecordTypeCode());    // 1
        batchControl.setServiceClassCode(220);                           // 2
        batchControl.setEntryAddendaCount(2);                            // 3
        batchControl.setEntryHash(new BigInteger("123"));           // 4
        batchControl.setTotalDebits(new BigDecimal("123.12"));      // 5
        batchControl.setTotalCredits(new BigDecimal("123.12"));      // 6
        batchControl.setCompanyIdentification("0000000000");            // 7
        assertNull(batchControl.getMessageAuthenticationCode()); // 8
        assertEquals("      ", batchControl.getReserved()); // 9
        batchControl.setOriginatingDfiIdentification("02100002"); // 10
        batchControl.setBatchNumber(1);         // 11

        //////////////////////////////
        // File Control Record
        //////////////////////////////
        FileControl fileControl = new FileControl();
        assertEquals("9", fileControl.getRecordTypeCode());
        fileControl.setBatchCount(1);
        fileControl.setBlockCount(1);
        fileControl.setEntryAddendaCount(1);
        fileControl.setEntryHashTotals(1l);
        fileControl.setTotalDebits(new BigDecimal("1.23"));
        fileControl.setTotalCredits(new BigDecimal("2.34"));
        assertEquals("                                       ", fileControl.getReserved());


        ACH ach = new ACH();



        ACHBatch achBatch = new ACHBatch();
        achBatch.setBatchHeader(batchHeader);
        achBatch.setBatchControl(batchControl);

        ACHBatchDetail batchDetail = new ACHBatchDetail();
        batchDetail.setDetailRecord(entryDetail);

        List<AddendaRecord> addendaRecords = new ArrayList<>();
        addendaRecords.add(addendaRecord);
        batchDetail.setAddendaRecords(addendaRecords);


        List<ACHBatchDetail> batchDetails = new ArrayList<>();
        batchDetails.add(batchDetail);
        achBatch.setDetails(batchDetails);


        ACHDocument doc = new ACHDocument();
        doc.setFileHeader(header);

        List<ACHBatch> achBatches = new ArrayList<>();
        achBatches.add(achBatch);
        doc.setBatches(achBatches);

        doc.setFileControl(fileControl);

        ACHWriter writer = ach.getWriter();
        String out = writer.write(doc);

        File file = new File("/Users/masanobuhoriyama/Desktop/nacha.txt");

        try (FileOutputStream fos = new FileOutputStream(file);) {
            writer.write(doc, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(out);

    }


}
