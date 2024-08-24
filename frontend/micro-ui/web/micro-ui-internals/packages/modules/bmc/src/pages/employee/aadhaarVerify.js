import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import SearchApplications from "../../components/SearchApplications";
import Title from "../../components/title";
import Pagination from "../../components/pagination";
import { Modal } from "@egovernments/digit-ui-react-components";

const headers = ["Select", "Name", "Application Number", "Ward Name", "Gender", "Pincode"];

const AadhaarVerifyPage = ({ Comment }) => {
  const { t } = useTranslation();
  const availableActions = ["VERIFY"];
  const [applications, setApplications] = useState(() => {
    const savedApplications = sessionStorage.getItem("applications");
    return savedApplications ? JSON.parse(savedApplications) : [];
  });
  const [currentPage, setCurrentPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(15);
  const [selectedRows, setSelectedRows] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedAction, setSelectedAction] = useState(availableActions[0]);

  const indexOfLastRow = currentPage * rowsPerPage;
  const indexOfFirstRow = indexOfLastRow - rowsPerPage;
  const currentRows = applications.slice(indexOfFirstRow, indexOfLastRow);
  const getVerifierApplications = Digit.Hooks.bmc.useVerifierSchemeDetail();
  const getVerifyScheme = Digit.Hooks.bmc.useVerifierScheme();

  useEffect(() => {
    return () => {
      sessionStorage.removeItem("applications");
    };
  }, []);

  const handleSearchCriteria = (criteria) => {
    getVerifierApplications.mutate(
      { schemeId: criteria.schemeID, detailID: criteria.detailID, type: criteria.type, action: "APPLY" },
      {
        onSuccess: (data) => {
          if (data && data.Applications) {
            setApplications(data.Applications);
            sessionStorage.setItem("applications", JSON.stringify(data.Applications));
          } else {
            setApplications([]);
            sessionStorage.setItem("applications", JSON.stringify([]));
          }
        },
        onError: (err) => {
          console.error("Error fetching applications:", err);
        },
      }
    );
  };

  const handleRowClick = (applicationNumber) => {
    window.location.href = `/digit-ui/employee/bmc/aadhaarEmployee/${applicationNumber}`;
  };

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

  const handleVerifyAll = () => {
    const data = {
      ApplicationStatus: {
        action: selectedAction,
        Comment: Comment || "",
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

  return (
    <React.Fragment>
      <Title text={t(`BMC_Application_for_${selectedAction}`)} />
      <div className="bmc-card-full">
        <SearchApplications onUpdate={handleSearchCriteria} />
        <div className="bmc-row-card-header" style={{ padding: "0" }}>
          <div className="bmc-card-row">
            <div className="bmc-table-container">
              <table className="bmc-hover-table">
                <thead>
                  <tr>
                    <th>
                      <input
                        type="checkbox"
                        onChange={handleSelectAll}
                        checked={currentRows.length > 0 && currentRows.every((row) => selectedRows.includes(row.applicationNumber))}
                      />
                    </th>
                    {headers.slice(1).map((header, index) => (
                      <th key={index} scope="col">
                        {header}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {currentRows.length > 0 ? (
                    currentRows.map((item, index) => (
                      <tr key={index} style={{ cursor: "pointer" }}>
                        <td>
                          <input
                            type="checkbox"
                            checked={selectedRows.includes(item.applicationNumber)}
                            onChange={() => handleRowCheckboxChange(item.applicationNumber)}
                          />
                        </td>
                        <td onClick={() => handleRowClick(item.applicationNumber)} style={{ color: "#F47738" }}>
                          {item.ApplicantDetails[0]?.AadharUser?.aadharName || "N/A"}
                        </td>
                        <td onClick={() => handleRowClick(item.applicationNumber)}>{item.applicationNumber || "N/A"}</td>
                        <td onClick={() => handleRowClick(item.applicationNumber)}>{item.ApplicantDetails[0]?.address?.wardName || "N/A"}</td>
                        <td onClick={() => handleRowClick(item.applicationNumber)}>{item.ApplicantDetails[0]?.AadharUser?.gender || "N/A"}</td>
                        <td onClick={() => handleRowClick(item.applicationNumber)}>{item.ApplicantDetails[0]?.address?.pinCode || "N/A"}</td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan={headers.length}>No data available</td>
                    </tr>
                  )}
                </tbody>
              </table>
              <Pagination
                totalRecords={applications.length}
                rowsPerPage={rowsPerPage}
                currentPage={currentPage}
                onPageChange={setCurrentPage}
                onRowsPerPageChange={setRowsPerPage}
              />
            </div>
            <div style={{ textAlign: "end", paddingBottom: "1rem", paddingRight: "1rem" }}>
              <button
                className="bmc-card-button"
                onClick={openModal}
                disabled={selectedRows.length === 0}
                style={{ backgroundColor: selectedRows.length === 0 ? "grey" : "#F47738" }}
              >
                {t(`BMC_Verify_All`)}
              </button>
            </div>
            {isModalOpen && (
              <div className="bmc-modal">
                <Modal onClose={closeModal} fullScreen hideSubmit={true} headerBarEnd={<CloseBtn onClick={closeModal} />}>
                  <p style={{ fontSize: "15px" }}>
                    <strong>You are verifying all the Applications without Review!</strong>
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
                    Submit
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
                    Cancel
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

export default AadhaarVerifyPage;
