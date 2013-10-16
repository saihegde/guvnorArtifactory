package gov.utah.dts.erep.guvnorartifactory.utils.impl;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class DiagnosisReportFormatter extends Formatter{

    /* (non-Javadoc)
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    @Override
    public String format(LogRecord record) {
        return formatMessage(record);
    }

}
