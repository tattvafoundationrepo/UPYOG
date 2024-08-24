import React, { useCallback, useState } from "react";
import { useLocation, useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import AddressDetailCard from "../components/AddressDetails";
import BankDetails from "../components/BankDetails";
import DisabilityCard from "../components/DisabilityCard";
import PersonalDetailCard from "../components/PersonalDetails";
import QualificationCard from "../components/QualificationCard";
// import WorkflowActions from "../components/Workflow";
import Timeline from "../components/bmcTimeline";
import Title from "../components/title";
import SchemeDetailsPage from "../components/schemeDetails";
import { Modal } from "@egovernments/digit-ui-react-components";

const splitMessage = (message, maxLength) => {
  const words = message.split(" ");
  let line = "";
  const result = [];

  for (const word of words) {
    if ((line + word).length > maxLength) {
      result.push(line.trim());
      line = "";
    }
    line += word + " ";
  }

  if (line.length) {
    result.push(line.trim());
  }

  return result.join("\n");
};

const BMCReviewPage = () => {
  const location = useLocation();
  const history = useHistory();
  const { t } = useTranslation();
  const { scheme, schemeType, updateSchemeData, selectedScheme } = location.state || {};
  const userDetails = Digit.UserService.getUser();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [userDetail, setUserDetail] = useState({});
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [applicationNumber, setApplicationNumber] = useState(null);
  const [messages, setMessages] = useState(null);

  const userFunction = (data) => {
    if (data && data.UserDetails && data.UserDetails.length > 0) {
      setUserDetail(data.UserDetails[0]);
    }
  };

  const getUserDetails = { UserSearchCriteria: { Option: "full", TenantID: tenantId, UserID: userDetails?.info?.id } };
  Digit.Hooks.bmc.useUsersDetails(getUserDetails, { select: userFunction });

  const saveSchemeDetail = Digit.Hooks.bmc.useSaveSchemes();

  const handleSaveData = useCallback(() => {
    const data = { SchemeApplications: { scheme, schemeType, updateSchemeData } };
    saveSchemeDetail.mutate(data, {
      onSuccess: (response) => {
        const originalMessage = response?.message || "";
        const responseMessage = originalMessage.toUpperCase();

        const formattedMessage = splitMessage(responseMessage, 5);

        const applicationNumber = response?.userSchemeApplication?.applicationNumber;

        if (formattedMessage) {
          setMessages(formattedMessage);
          setApplicationNumber(null);
        } else if (applicationNumber) {
          setApplicationNumber(applicationNumber);
          setMessages(null);
        }

        setIsModalOpen(true);
      },
      onError: (error) => {
        console.error("Failed to save user details:", error);
      },
    });
  }, [updateSchemeData, scheme, schemeType, saveSchemeDetail]);

  const handleCallback = useCallback((data) => {
    console.log(data);
  }, []);

  const handleModalSave = () => {
    history.push({
      pathname: "/digit-ui/citizen",
    });
  };

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        {window.location.href.includes("/citizen") ? <Timeline currentStep={4} /> : null}
        <Title text={"Review Application"} />
        <PersonalDetailCard onUpdate={handleCallback} initialRows={userDetail} tenantId={tenantId} AllowEdit={false} />
        <AddressDetailCard onUpdate={handleCallback} initialRows={userDetail} tenantId={tenantId} AllowEdit={false} />
        <QualificationCard
          initialRows={userDetail.UserQualification}
          tenantId={tenantId}
          AddOption={false}
          AllowRemove={false}
          onUpdate={handleCallback}
        />
        <BankDetails onUpdate={handleCallback} initialRows={userDetail.UserBank} tenantId={tenantId} AddOption={false} AllowRemove={false} />
        <DisabilityCard onUpdate={handleCallback} initialRows={userDetail.divyang} tenantId={tenantId} AllowEdit={false} />
        <SchemeDetailsPage
          onUpdate={handleCallback}
          initialRows={updateSchemeData || userDetail}
          tenantId={tenantId}
          AllowEdit={false}
          scheme={scheme}
          schemeType={schemeType}
          selectedScheme={selectedScheme}
        />
        {window.location.href.includes("/citizen") && (
          <div className="bmc-card-row" style={{ textAlign: "end" }}>
            <button
              type="submit"
              className="bmc-card-button"
              style={{
                borderBottom: "3px solid black",
                outline: "none",
              }}
              onClick={handleSaveData}
            >
              {t("BMC_Save")}
            </button>
          </div>
        )}
      </div>

      {isModalOpen && (
        <div className="bmc-modal">
          <Modal fullScreen hideSubmit={true}>
            {messages ? (
              <React.Fragment>
                <p style={{ fontSize: "20px" }}>
                  <strong>Sorry, Your Application Is not Eligible!</strong>
                </p>
                <p>
                  {messages.split("\n").map((line, index) => (
                    <React.Fragment key={index}>
                      {line}
                      <br />
                    </React.Fragment>
                  ))}
                </p>
              </React.Fragment>
            ) : (
              <React.Fragment>
                <p style={{ fontSize: "20px" }}>
                  <strong>Application Submitted Successfully</strong>
                </p>
                {applicationNumber && <p>Your Application Number is: {applicationNumber}</p>}
              </React.Fragment>
            )}
            <button onClick={handleModalSave} className="bmc-card-button">
              OK
            </button>
          </Modal>
        </div>
      )}
    </React.Fragment>
  );
};

export default BMCReviewPage;
