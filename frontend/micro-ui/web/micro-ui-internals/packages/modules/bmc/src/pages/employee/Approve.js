import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import SearchApplications from "../../components/SearchApplications";
import Title from "../../components/title";
import { CheckBox, Modal, Table } from "@upyog/digit-ui-react-components";
import { useHistory } from "react-router-dom";
import CustomTable from "../../components/CustomTable";

const ApprovePage = ({ Comment, isLoading }) => {
  const { t } = useTranslation();
  const actions = ["SELECTED", "VERIFY"];
  const availableActions = ["SELECTED", "VERIFY"];
  const history = useHistory();
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [selectedRows, setSelectedRows] = useState([]);
  const [totalRecords, setTotalRecords] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [applications, setApplications] = useState(() => {
    const savedApplications = sessionStorage.getItem("applications");
    return savedApplications ? JSON.parse(savedApplications) : [];
  });
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedAction, setSelectedAction] = useState(availableActions[0]);

  const getVerifierApplications = Digit.Hooks.bmc.useVerifierSchemeDetail();
  const getVerifyScheme = Digit.Hooks.bmc.useVerifierScheme();

  const indexOfLastRow = currentPage * rowsPerPage;
  const indexOfFirstRow = indexOfLastRow - rowsPerPage;
  const currentRows = applications.slice(indexOfFirstRow, indexOfLastRow);

  const handleSearchCriteria = (criteria) => {
    getVerifierApplications.mutate(
      { schemeId: criteria.schemeID, detailID: criteria.detailID, type: criteria.type, action: availableActions },
      {
        onSuccess: (data) => {
          if (data && data.Applications) {
            setApplications(data.Applications);
            setTotalRecords(data.Applications.length);
            sessionStorage.setItem("applications", JSON.stringify(data.Applications));
          } else {
            setApplications([]);
            setTotalRecords(0);
            sessionStorage.setItem("applications", JSON.stringify([]));
          }
        },
        onError: (err) => {
          console.error("Error fetching applications:", err);
        },
      }
    );
  };

  const handleVerifyAll = () => {
    const data = {
      ApplicationStatus: {
        action: "APPROVE",
        Comment: { value: null, label: null },
        ApplicationNumbers: selectedRows || [],
      },
    };

    getVerifyScheme.mutate(data, {
      onSuccess: () => {
        setApplications((prevApplications) => prevApplications.filter((application) => !selectedRows.includes(application.applicationNumber)));
        setSelectedRows([]);
        setIsModalOpen(false);
      },
      onError: (err) => {
        console.error("Error verifying applications:", err);
      },
    });
  };

  useEffect(() => {
    return () => {
      sessionStorage.removeItem("applications");
    };
  }, []);

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      const allApplicationNumbers = currentRows.map((row) => row.applicationNumber);
      setSelectedRows(allApplicationNumbers);
    } else {
      setSelectedRows([]);
    }
  };

  const handleRowCheckboxChange = (applicationNumber) => {
    setSelectedRows((prevSelectedRows) =>
      prevSelectedRows.includes(applicationNumber)
        ? prevSelectedRows.filter((row) => row !== applicationNumber)
        : [...prevSelectedRows, applicationNumber]
    );
  };

  const handleRowClick = (applicationNumber) => {
    history.push(`/digit-ui/employee/bmc/aadhaarEmployee/${applicationNumber}`, {
      applicationNumber: applicationNumber,
      applications: applications,
    });
  };

  const handleNextPage = () => {
    if (currentPage * rowsPerPage < totalRecords) {
      setCurrentPage((prev) => prev + 1);
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 1) {
      setCurrentPage((prev) => prev - 1);
    }
  };

  const handlePageSizeChange = (event) => {
    const newSize = Number(event.target.value);
    if (newSize && !isNaN(newSize)) {
      setRowsPerPage(newSize);
      setCurrentPage(1);
    }
  };

  const openModal = () => {
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
  };

  const Close = () => (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
      <path d="M0 0h24v24H0V0z" fill="none" />
      <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
    </svg>
  );

  const CloseBtn = (props) => {
    return (
      <div className="icon-bg-secondary" onClick={props.onClick}>
        <Close />
      </div>
    );
  };

  const Tablecolumns = [
    {
      Header: (
        <CheckBox
          onChange={handleSelectAll}
          checked={currentRows.length > 0 && currentRows.every((row) => selectedRows.includes(row.applicationNumber))}
        />
      ),
      accessor: `checkbox`,
      Cell: ({ row }) => {
        const isChecked = selectedRows.includes(row.original.applicationNumber);
        return <CheckBox checked={isChecked} onChange={() => handleRowCheckboxChange(row.original.applicationNumber)} />;
      },
      disableSortBy: true,
    },
    {
      Header: t("Name"),
      accessor: "aadharName",
      Cell: ({ row }) => (
        <div style={{ cursor: "pointer", color: "#F47738" }} onClick={() => handleRowClick(row.original.applicationNumber)}>
          {row.original.aadharName}
        </div>
      ),
    },
    {
      Header: t("Application Number"),
      accessor: "applicationNumber",
      Cell: ({ row }) => (
        <div style={{ cursor: "pointer" }} onClick={() => handleRowClick(row.original.applicationNumber)}>
          {row.original.applicationNumber}
        </div>
      ),
    },
    {
      Header: t("BMC_WARD_NAME"),
      accessor: "ward",
      Cell: ({ row }) => (
        <div style={{ cursor: "pointer" }} onClick={() => handleRowClick(row.original.applicationNumber)}>
          {row.original.ward}
        </div>
      ),
    },
    {
      Header: t("Gender"),
      accessor: "gender",
      Cell: ({ row }) => (
        <div style={{ cursor: "pointer" }} onClick={() => handleRowClick(row.original.applicationNumber)}>
          {row.original.gender}
        </div>
      ),
    },
    {
      Header: t("Pincode"),
      accessor: "pincode",
      Cell: ({ row }) => (
        <div style={{ cursor: "pointer" }} onClick={() => handleRowClick(row.original.applicationNumber)}>
          {row.original.pincode}
        </div>
      ),
    },
  ];

  const filteredData = applications.slice(indexOfFirstRow, indexOfLastRow).map((application) => {
    const applicantDetails = application.ApplicantDetails?.[0];
    return {
      checkbox: (
        <CheckBox
          checked={selectedRows.includes(application.applicationNumber)}
          onChange={() => handleRowCheckboxChange(application.applicationNumber)}
        />
      ),
      aadharName: applicantDetails?.AadharUser?.aadharName || "N/A",
      applicationNumber: application.applicationNumber || "N/A",
      ward: applicantDetails?.UserOtherDetails?.ward || "N/A",
      gender: applicantDetails?.AadharUser?.gender || "N/A",
      pincode: applicantDetails?.address?.pincode || "N/A",
    };
  });

  return (
    <React.Fragment>
      <Title text={t(`BMC_Application_for_${selectedAction}`)} />
      <div className="bmc-card-full">
        <SearchApplications onUpdate={handleSearchCriteria} actions={actions} />
        <div className="bmc-row-card-header" style={{ padding: "0" }}>
          <div className="bmc-card-row">
            <div className="bmc-table-container">
              <CustomTable
                t={t}
                columns={Tablecolumns}
                data={filteredData || []}
                manualPagination={false}
                totalRecords={totalRecords}
                isLoadingRows={isLoading}
                getCellProps={(cellInfo) => ({
                  style: {
                    fontSize: "16px",
                  },
                })}
              />
            </div>
            <div style={{ textAlign: "end", paddingBottom: "1rem", paddingRight: "1rem" }}>
              <button
                className="bmc-card-button"
                onClick={openModal}
                disabled={selectedRows.length === 0}
                style={{ backgroundColor: selectedRows.length === 0 ? "grey" : "#F47738" }}
              >
                {t(`BMC_APPROVE_ALL`)}
              </button>
            </div>
            {isModalOpen && (
              <div className="bmc-modal">
                <Modal onClose={closeModal} fullScreen hideSubmit={true} headerBarEnd={<CloseBtn onClick={closeModal} />}>
                  <p style={{ fontSize: "15px" }}>
                    <strong>{t("You are Approving all the Applications without Review!")}</strong>
                  </p>
                  <button
                    style={{
                      backgroundColor: "#F47738",
                      width: "91px",
                      height: "34px",
                      color: "white",
                      marginTop: "1.5rem",
                      borderBottom: "3px solid black",
                      outline: "none",
                      margin: "8px",
                    }}
                    onClick={handleVerifyAll}
                  >
                    {t("Submit")}
                  </button>
                  <button
                    style={{
                      backgroundColor: "grey",
                      width: "91px",
                      height: "34px",
                      color: "white",
                      marginTop: "1.5rem",
                      borderBottom: "3px solid black",
                      outline: "none",
                    }}
                    onClick={closeModal}
                  >
                    {t("Cancel")}
                  </button>
                </Modal>
              </div>
            )}
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default ApprovePage;
