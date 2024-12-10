import React, { useCallback, useState } from "react";
import { useLocation, useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import AddressDetailCard from "../components/AddressDetails";
import BankDetails from "../components/BankDetails";
import DisabilityCard from "../components/DisabilityCard";
import PersonalDetailCard from "../components/PersonalDetails";
import QualificationCard from "../components/QualificationCard";
import Timeline from "../components/bmcTimeline";
import Title from "../components/title";
import SchemeDetailsPage from "../components/schemeDetails";
import { Modal } from "@upyog/digit-ui-react-components";
import DocumentCard from "../components/DocumentCard";
import UserOthersDetails from "../components/userOthersDetails";

const BMCReviewPage = () => {
  const location = useLocation();
  const history = useHistory();
  const { t } = useTranslation();
  const { scheme, schemeType, updateSchemeData, selectedScheme } = location.state || {};
  const userDetails = Digit.UserService.getUser();
  const tenantId = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.code || Digit.ULBService.getCurrentTenantId();
  const [userDetail, setUserDetail] = useState({});
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [applicationNumber, setApplicationNumber] = useState(null);
  const [messages, setMessages] = useState(null);

  const userFunction = (data) => {
    if (data && data.UserDetails && data.UserDetails.length > 0) {
      setUserDetail(data.UserDetails[0]);
    }
  };

  const data = updateSchemeData;
  const checkValue = {};
  checkValue.UserOtherDetails = {};
  checkValue.UserOtherDetails = data.UserOtherDetails;
  checkValue.documentDetails = {};
  checkValue.documentDetails.CommonDetails = data.documents;
  checkValue.occupation = data?.occupation?.value;
  checkValue.income = data?.income?.value;
  checkValue.statement = data.statement;
  checkValue.agreeToPay = data.agreeToPay;
  checkValue.employed = data.employed;

  const getUserDetails = { UserSearchCriteria: { Option: "full", TenantID: tenantId, UserID: userDetails?.info?.id } };
  Digit.Hooks.bmc.useUsersDetails(getUserDetails, { select: userFunction });

  const saveSchemeDetail = Digit.Hooks.bmc.useSaveSchemes();

  const handleSaveData = useCallback(() => {
    const data = { SchemeApplications: { scheme, schemeType, updateSchemeData, tenantId } };
    saveSchemeDetail.mutate(data, {
      onSuccess: (response) => {
        let message = response?.message ? response.message : null;
        const applicationNumber = response?.userSchemeApplication?.applicationNumber;
  
        if (message) {
          const words = message.split(" ");
          if (words.length >= 5) {
            const modifiedWords = [...words.slice(0, 5), t(scheme?.label), ...words.slice(5)];
  
            modifiedWords.splice(modifiedWords.length - 3, 2);
  
            message = modifiedWords.map(word => t(word)).join(" "); 
          }
          setMessages(t(message));
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
  }, [updateSchemeData, scheme, schemeType, saveSchemeDetail, tenantId, t]);


  const handleCallback = useCallback((data) => {
    console.log(data);
  }, []);

  const handleModalSave = () => {
    history.push({
      pathname: "/digit-ui/citizen/bmc/bmc-home",
    });
  };

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        {window.location.href.includes("/citizen") ? <Timeline currentStep={4} /> : null}
        <Title text={`${t("Review Application")} - ${t(scheme?.label)}`} />
        <PersonalDetailCard onUpdate={handleCallback} initialRows={userDetail} tenantId={tenantId} AllowEdit={false} />
        <AddressDetailCard onUpdate={handleCallback} initialRows={userDetail} tenantId={tenantId} AllowEdit={false} />
        <QualificationCard
          initialRows={userDetail.UserQualification}
          tenantId={tenantId}
          AddOption={false}
          AllowRemove={false}
          AllowEdit={false}
          onUpdate={handleCallback}
        />
        <BankDetails onUpdate={handleCallback} initialRows={userDetail.UserBank || []} tenantId={tenantId} AddOption={false} AllowRemove={false}
          AllowEdit={false} />
        <DisabilityCard onUpdate={handleCallback} initialRows={userDetail.divyang} tenantId={tenantId} AllowEdit={false} />
        <DocumentCard
          onUpdate={handleCallback}
          initialRows={userDetail.documentDetails}
          tenantId={tenantId}
          AddOption={false}
          AllowEdit={false}
          AllowRemove={false}

        ></DocumentCard>

        <UserOthersDetails onUpdate={handleCallback} initialRows={userDetail.UserOtherDetails} tenantId={tenantId} AllowEdit={false} />
        <SchemeDetailsPage
          onUpdate={handleCallback}
          initialRows={checkValue}
          tenantId={tenantId}
          AllowEdit={false}
          scheme={scheme}
          schemeType={schemeType}
          selectedScheme={selectedScheme}
        />
        {window.location.href.includes("/citizen") && (
          <div className="bmc-card-row" style={{ textAlign: "end", paddingBottom: "1rem" }}>
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
                  <strong>{t("Sorry Your Application Is not Eligible!")}</strong>
                </p>
                <p>{messages}</p>
              </React.Fragment>
            ) : (
              <React.Fragment>
                <p style={{ fontSize: "20px" }}>
                  <strong>{t("Application Submitted Successfully")}</strong>
                </p>
                {applicationNumber && (
                  <p>
                    {t("Your Application Number is")}: {applicationNumber}
                  </p>
                )}
              </React.Fragment>
            )}
            <button onClick={handleModalSave} className="bmc-card-button">
              {t("OK")}
            </button>
          </Modal>
        </div>
      )}
    </React.Fragment>
  );
};

export default BMCReviewPage;
