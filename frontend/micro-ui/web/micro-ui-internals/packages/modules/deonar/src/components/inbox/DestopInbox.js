import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import InboxLinks from "../inbox/ApplicationLinks";
import CustomTable from "../../pages/employee/commonFormFields/customTable";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";

const DesktopInbox = ({ tableConfig, filterComponent, ...props }) => {
  const { t } = useTranslation();
  const tenantIds = Digit.SessionStorage.get("BMC_TENANTS");
  const [totalRecords, setTotalRecords] = useState(0);

  const [FilterComponent, setComp] = useState(() => Digit.ComponentRegistryService?.getComponent(filterComponent));

  const { fetchEntryFeeDetailsbyUUID } = useDeonarCommon();

  const { data: fetchedData, isLoading } = fetchEntryFeeDetailsbyUUID({ forCollection: true });
  const [animalCount, setAnimalCount] = useState([]);

  const baseColumns = [
    {
      Header: "ID",
      accessor: "id",
      Cell: ({ row }) => row.index + 1,
      isVisible: false,
    },
    {
      Header: "DEONAR_ARRIVAL_UUID",
      accessor: "entryUnitId",
      Cell: ({ row }) => (
        <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
          {row.original.entryUnitId}
        </span>
      ),
    },
    {
      Header: "DEONAR_TRADER_NAME",
      accessor: "traderName",
    },
    {
      Header: "DEONAR_LICENSE_NUMBER",
      accessor: "licenceNumber",
    },
    {
      Header: "ARRIVAL_DATE_FIELD",
      accessor: "dateOfArrival",
    },
    {
      Header: "ARRIVAL_TIME_FIELD",
      accessor: "timeOfArrival",
    },
  ];

  useEffect(() => {
    if (fetchedData) {
      setAnimalCount(fetchedData.SecurityCheckDetails || []);
      setTotalRecords();
    }
  }, [fetchedData]);

  const transformedAnimalCount = animalCount.map((item) => ({
    entryUnitId: item.entryUnitId,
    traderName: item.traderName,
    licenceNumber: item.licenceNumber,
    dateOfArrival: item.dateOfArrival,
    timeOfArrival: item.timeOfArrival,
  }));

  return (
    <div className="inbox-container" style={{ overflow: "auto", scrollbarWidth: "none", msOverflowStyle: "none" }}>
      {!props.isSearch && (
        <div className="filters-container">
          <InboxLinks
            parentRoute={props.parentRoute}
            allLinks={[
              {
                text: "BMC_MASTER",
                link: `/digit-ui/employee/deonar/create`,
                businessService: "deonar",
                roles: ["SUPERUSER"],
              },
            ]}
            headerText={"DEONAR"}
            businessService={props.businessService}
          />
          <div>
            {
              <FilterComponent
                defaultSearchParams={props.defaultSearchParams}
                onFilterChange={props.onFilterChange}
                searchParams={props.searchParams}
                type="desktop"
                tenantIds={tenantIds}
              />
            }
          </div>
        </div>
      )}
      {
        <div className="result" style={{ marginLeft: !props?.isSearch ? "24px" : "", flex: 1 }}>
          <CustomTable
            t={t}
            columns={baseColumns}
            data={transformedAnimalCount}
            manualPagination={false}
            totalRecords={totalRecords}
            sortParams={[]}
            config={[]}
            tableClassName={"ebe-custom-scroll"}
            isLoadingRows={isLoading}
            showSearch={true}
            showText={true}
            showPdfDownload={true}
            showExcelDownload={true}
            pdfButtonText="Download PDF"
            excelButtonText="Download Excel"
            customDownloadButtonStyles={{
              fontSize: "14px",
              fontWeight: "bold",
            }}
          />
        </div>
      }
    </div>
  );
};

export default DesktopInbox;
