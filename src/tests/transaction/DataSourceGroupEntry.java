/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Technologies. Exolab is a registered
 *    trademark of Intalio Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2000 (C) Intalio Technologies Inc. All Rights Reserved.
 *
 */


package transaction;

import java.util.ArrayList;
import transaction.configuration.Datasource;
import transaction.configuration.Performance;

/////////////////////////////////////////////////////////////////////
// DataSourceGroupEntry
/////////////////////////////////////////////////////////////////////

/**
 * Collects information needed for testing a group of 
 * {@link DataSourceEntry}
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
final class DataSourceGroupEntry {

    /**
     * The group name (optional)
     */
    private final String _groupName;

    /**
     * The performance
     */
    private final Performance _performance;

    /**
     * The list of {@link DataSourceEntry}
     */
    private final ArrayList _datasourceEntries;

    /**
     * Create the DataSourceGroupEntry
     *
     * @param groupName the group name (optional)
     * @param performance the performance (optional)
     */
    DataSourceGroupEntry(String groupName, Performance performance) {
        _groupName = groupName;
        _performance = performance;
        _datasourceEntries = new ArrayList();
    }

    /**
     * Return true if the group is bigger than one.
     *
     * @return true if the group is bigger than one.
     */
    boolean hasMultiple() {
        return _datasourceEntries.size() > 1;
    }

    /**
     * Return the group name (optional)
     *
     * @return the group name
     */
    String getGroupName() {
        return _groupName;
    }

    /**
     * Return the performance (optional)
     *
     * @return the performance (optional)
     */
    Performance getPerformance() {
        return _performance;
    }

    /**
     * Return the number of data source entries.
     *
     * @return the number of data source entries.
     */
    int getNumberOfDataSourceEntries() {
        return _datasourceEntries.size();
    }

    /**
     * Return the data soruce entry at the specified index.
     *
     * @param index the index
     * @return the data soruce entry at the specified index.
     */
    DataSourceEntry getDataSourceEntry(int index) {
        return (DataSourceEntry)_datasourceEntries.get(index);
    }

    /**
     * Add the {@link DataSourceEntry}.
     *
     * @param dataSourceEntry the {@link DataSourceEntry) (required)
     */
    void addDataSourceEntry(DataSourceEntry dataSourceEntry) {
        if (null == dataSourceEntry) {
            throw new IllegalArgumentException("The argument 'dataSourceEntry' is null.");
        }
        _datasourceEntries.add(dataSourceEntry);
    }

}
