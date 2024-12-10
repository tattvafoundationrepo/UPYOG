import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { useHistory } from "react-router-dom";
import { columns, useDebounce } from "../collectionPoint/utils";
import CustomTable from "../commonFormFields/customTable";
import useDeonarCommon from "../../../../../../libraries/src/hooks/deonar/useCommonDeonar";

const AnteMortemPreSlaughterInspectionPage = () => {
  const { t } = useTranslation();
  const history = useHistory();
  // const tenantId = Digit.ULBService.getCurrentTenantId();

  const [selectedUUID, setSelectedUUID] = useState("");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [totalRecords, setTotalRecords] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSizeLimit, setPageSizeLimit] = useState(10);
  const [sortParams, setSortParams] = useState({});
  const [animalCount, setAnimalCount] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");

  const { fetchEntryFeeDetailsbyUUID } = useDeonarCommon();
  const { data: fetchedData } = fetchEntryFeeDetailsbyUUID({});

  const debouncedSearchTerm = useDebounce(searchTerm, 300);
  useEffect(() => {
    if (fetchedData) {
      setAnimalCount(fetchedData.SecurityCheckDetails);
    }
  }, [fetchedData]);

  const handleNextPage = () => {
    if (currentPage * pageSizeLimit < totalRecords) {
      setCurrentPage((prev) => prev + 1);
      console.log("Next page clicked:", currentPage, pageSizeLimit, totalRecords);
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 1) {
      setCurrentPage((prev) => prev - 1);
    }
  };

  const handlePageSizeChange = (event) => {
    const newSize = event.target.value;
    const pageSize = Number(newSize);

    if (pageSize && !isNaN(pageSize)) {
      setPageSizeLimit(pageSize);
      setCurrentPage(1); // Reset to first page
    } else {
      console.error("Invalid page size value:", newSize);
    }
  };

  const filteredData = animalCount.filter((row) => {
    const values = Object.values(row);
    return values.some((value) => String(value).toLowerCase().includes(debouncedSearchTerm.toLowerCase()));
  });

  const handleSearch = (searchTerm) => {
    setSearchTerm(searchTerm);
  };

  const handleUUIDClick = (entryUnitId) => {
    const selectedData = animalCount.find((item) => item.entryUnitId === entryUnitId);
    setSelectedUUID(entryUnitId);
    setIsModalOpen(!isModalOpen);

    history.push(`/digit-ui/employee/deonar/antemortermpreslaughterinspectionstatus/${entryUnitId}`, {
      entryUnitId: entryUnitId,
      inspectionData: selectedData || {},
    });
  };

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <MainFormHeader title={t("DEONAR_BEFORE_SLAUGHTER_ANTE_MORTEM_INSPECTION")} />
        <div className="bmc-row-card-header">
          <div className="bmc-card-row">
            <CustomTable
              t={t}
              columns={columns(handleUUIDClick)}
              data={filteredData}
              tableClassName={"deonar-custom-scroll"}
              currentPage={currentPage}
              pageSizeLimit={pageSizeLimit}
              totalRecords={totalRecords}
              searchTerm={debouncedSearchTerm}
              onSearchChange={handleSearch}
              onNextPage={handleNextPage}
              onPrevPage={handlePrevPage}
              onPageSizeChange={handlePageSizeChange}
              sortParams={sortParams}
            />
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default AnteMortemPreSlaughterInspectionPage;
