import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useHistory } from "react-router-dom";
import SchemeDetailsPage from "../components/schemeDetails";
import Timeline from "../components/bmcTimeline";
import Title from "../components/title";

const ApplicationDetailFull = (_props) => {
  const { t } = useTranslation();
  const location = useLocation();
  const history = useHistory();
  const tenantId = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.code || Digit.ULBService.getCurrentTenantId();
  const userDetails = Digit.UserService.getUser();
  const { scheme, schemeType, selectedScheme } = location.state || {};

  const [userDetail, setUserDetail] = useState({});
  const [updateSchemeData, setUpdateSchemeData] = useState({});
  const [checkboxStates, setCheckboxStates] = useState({ agreeToPay: false, statement: false });

  const userFunction = (data) => {
    if (data && data.UserDetails && data.UserDetails.length > 0) {
      setUserDetail(data.UserDetails[0]);
    }
  };

  const getUserDetails = { UserSearchCriteria: { Option: "full", TenantID: tenantId, UserID: userDetails?.info?.id } };
  Digit.Hooks.bmc.useUsersDetails(getUserDetails, { select: userFunction });

  const handleApplicationUpdate = (updatedData) => {
    setUpdateSchemeData(updatedData);
  };

  const handleCheckboxChange = (name, checked) => {
    setCheckboxStates((prev) => ({ ...prev, [name]: checked }));
  };

  const handleConfirmClick = () => {
    
    history.push({
      pathname: "/digit-ui/citizen/bmc/review",
      state: {
        scheme,
        schemeType,
        selectedScheme,
        updateSchemeData,
      },
    });
    setTimeout(() => {
      window.location.reload();
    }, 1);
  };

  const isButtonEnabled = checkboxStates.agreeToPay && checkboxStates.statement;

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        {window.location.href.includes("/citizen") && <Timeline currentStep={4} />}
        <Title text={`${t("Application")} - ${t(scheme?.label)}`} />

        <SchemeDetailsPage
          onUpdate={handleApplicationUpdate}
          initialRows={userDetail}
          AllowEdit={true}
          tenantId={tenantId}
          scheme={scheme}
          selectedScheme={selectedScheme}
          schemeType={schemeType}
          checkboxStates={checkboxStates}
          onCheckboxChange={handleCheckboxChange}
        />
        <div className="bmc-card-row" style={{ textAlign: "end" }}>
          <button
            type="submit"
            className="bmc-card-button"
            style={{
              borderBottom: "3px solid black",
              outline: "none",
              backgroundColor: isButtonEnabled ? "#f47738" : "grey",
            }}
            onClick={isButtonEnabled ? handleConfirmClick : null}
          >
            {t("BMC_Confirm")}
          </button>
        </div>
      </div>
    </React.Fragment>
  );
};

export default ApplicationDetailFull;
