import { Card, CloseSvg, Loader } from "@upyog/digit-ui-react-components";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import InboxLinks from "../inbox/ApplicationLinks";
import ApplicationTable from "../inbox/ApplicationTable";
import SearchApplication from "./search";
import CustomTable from "../CustomTable";
import InteractiveCharts from "../InteractiveCharts";
import { useEffect } from "react";
import useBMCCommon from "@upyog/digit-ui-libraries/src/hooks/bmc/useBMCCommon";


const DesktopInbox = ({ tableConfig, filterComponent, ...props }) => {
  const { t } = useTranslation();
  const tenantIds = Digit.SessionStorage.get("BMC_TENANTS");
  const [totalRecords, setTotalRecords] = useState(0);
  const [sortParams, setSortParams] = useState({});
  const [getApplicationData, setGetApplicationData] = useState([]);

  const GetCell = (value) => <span className="cell-text">{t(value)}</span>;
  const GetSlaCell = (value) => {
    return value == "INACTIVE" ? <span className="sla-cell-error">{t(value) || ""}</span> : <span className="sla-cell-success">{t(value) || ""}</span>;
  };
  const data = props?.data?.Employees;

  const [FilterComponent, setComp] = useState(() => Digit.ComponentRegistryService?.getComponent(filterComponent));

  const { fetchBMCCommon } = useBMCCommon();
const { data: schemeBMCData } = fetchBMCCommon();

useEffect(() => {
  if (schemeBMCData) {
    
    const appData = schemeBMCData?.applications || [];

    
    const formattedApplications = appData.map((app) => ({
      id: app.ApplicationNumber || "N/A",
      name: app.Name || "N/A",
      schemeName:app.SchemeName || "N/A",
      machineName: app.MachineName || "N/A",
      courseName: app.courseName || "N/A",
      courseName:app.CourseNAme|| "N/A",
      data:app.CreatedDate || "N/A",
      status:app.state || "N/A",
      city: app.City || "N/A",
      zone: app.Zone || "N/A",
    }));

    setGetApplicationData(formattedApplications);
  }
}, [schemeBMCData]);


  const columns = React.useMemo(() => {
    return [
      {
        Header: t("HR_EMP_ID_LABEL"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return (
            <span className="link">
              <Link to={`/${window?.contextPath}/employee/bmc/details/${row.original.tenantId}/${row.original.code}`}>{row.original.code}</Link>
            </span>
          );
        },
      },
      {
        Header: t("HR_EMP_NAME_LABEL"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.user?.name}`);
        },
      },
      {
        Header: t("HR_ROLE_NO_LABEL"),
        Cell: ({ row }) => {
          return (
            <div className="tooltip">
              {" "}
              {GetCell(`${row.original?.user?.roles.length}`)}
              <span className="tooltiptext" style={{ whiteSpace: "nowrap" }}>
                {row.original?.user?.roles.map((ele, index) => (
                  <span>
                    {`${index + 1}. ` + t(`ACCESSCONTROL_ROLES_ROLES_${ele.code}`)} <br />{" "}
                  </span>
                ))}
              </span>
            </div>
          );
        },
        disableSortBy: true,
      },
      {
        Header: t("HR_DESG_LABEL"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(
            `${t(
              "COMMON_MASTERS_DESIGNATION_" + row.original?.assignments?.sort((a, b) => new Date(a.fromDate) - new Date(b.fromDate))[0]?.designation
            ) || ""
            }`
          );
        },
      },
      {
        Header: t("HR_DEPT_LABEL"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(
            `${t(
              "COMMON_MASTERS_DEPARTMENT_" + row.original?.assignments?.sort((a, b) => new Date(a.fromDate) - new Date(b.fromDate))[0]?.department
            ) || ""
            }`
          );
        },
      },
      {
        Header: t("HR_STATUS_LABEL"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetSlaCell(`${row.original?.isActive ? "ACTIVE" : "INACTIVE"}`);
        },
      },
    ];
  }, []);

  const visibleColumns = [
    {
      Header: t("BMC_ApplicationNumber"),
      accessor: "ApplicationNumber",
      Cell: ({ row }) => t(row.original.id) || "N/A",
    },
    {
      Header: t("Name"),
      accessor: "Name",
      Cell: ({ row }) => t(row.original.name) || "N/A",
    },
    {
      Header: t("BMC_SchemeName"),
      accessor: "SchemeName",
      Cell: ({ row }) => t(row.original.schemeName) || "N/A",
    },
    {
      Header: t("BMC_MachineName"),
      accessor: "MachineName",
      Cell: ({ row }) => t(row.original.machineName) || "N/A",
    },
    {
      Header: t("BMC_CourseName"),
      accessor: "CourseName",
      Cell: ({ row }) => t(row.original.courseName) || "N/A",
    },

    {
      Header: t("BMC_LastModifiedDate"),
      accessor: "CreatedDate",
      Cell: ({ row }) => t(row.original.data) || "N/A",
    },
    {
      Header: t("BMC_CurrentStatus"),
      accessor: "state",
      Cell: ({ row }) => t(row.original.status) || "N/A",
    },

    // {
    //   Header: t("BMC_Comment"),
    //   accessor: "comment",
    //   Cell: ({ value }) => t(value) || "N/A",
    // }
  ];

  const handleOnClick = (rowDocument) => {

  };
  const pieChartData = {
    labels: ['Red', 'Blue', 'Yellow'],
    datasets: [
      {
        label: 'Categories',
        data: [30, 45, 25],
        backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56'],
        hoverBackgroundColor: ['#FF6384', '#36A2EB', '#FFCE56'],
      },
    ],
  };

  const detailsData = {
    Red: {
      labels: ['Detail A', 'Detail B', 'Detail C'],
      data: [10, 20, 15],
    },
    Blue: {
      labels: ['Detail X', 'Detail Y', 'Detail Z'],
      data: [25, 30, 35],
    },
    Yellow: {
      labels: ['Detail 1', 'Detail 2', 'Detail 3'],
      data: [5, 10, 10],
    },
  };

  let result;


  return (
    <div className="inbox-container bmc-row-card-header" style={{ overflow: "auto", scrollbarWidth: "none", msOverflowStyle: "none" }}>
      {!props.isSearch && (
        <div className="filters-container">
          <InboxLinks
            parentRoute={props.parentRoute}
            allLinks={[
              {
                text: "BMC_MASTER",
                link: `/digit-ui/employee/bmc/create`,
                businessService: "bmc",
                roles: ["SUPERUSER"],
              },
            ]}
            headerText={"BMC"}
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
      {<div className="result" style={{ marginLeft: !props?.isSearch ? "24px" : "", flex: 1 }}>
        <div className="bmc-row-card-header">
        <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>



          <CustomTable
            t={t}
            pageSizeLimit={10}
            columns={visibleColumns}
            data={getApplicationData || []}
            manualPagination={false}
            totalRecords={totalRecords}
            sortParams={sortParams}
            config={[]}
            tableClassName={"ebe-custom-scroll"}
            // isLoadingRows={isLoading}
            showSearch={true}
            showText={true}


          />
          </div>
        </div>

        <InteractiveCharts pieChartData={pieChartData} detailsData={detailsData}></InteractiveCharts>
        {/* <SearchApplication
          defaultSearchParams={props.defaultSearchParams}
          onSearch={props.onSearch}
          type="desktop"
          tenantIds={tenantIds}
          searchFields={props.searchFields}
          isInboxPage={!props?.isSearch}
          searchParams={props.searchParams}
        />
        <div className="result" style={{ marginLeft: !props?.isSearch ? "24px" : "", flex: 1 }}>
          {result}
        </div> */}
      </div>}
    </div>
  );
};

export default DesktopInbox;
