import React, { useCallback } from "react";
import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import AddressDetailCard from "../../components/AddressDetails";
import BankDetails from "../../components/BankDetails";
import DisabilityCard from "../../components/DisabilityCard";
import PersonalDetailCard from "../../components/PersonalDetails";
import QualificationCard from "../../components/QualificationCard";
import WorkflowActions from "../../components/Workflow";
import Title from "../../components/title";
import SchemeDetailsPage from "../../components/schemeDetails";
import DocumentCard from "../../components/DocumentCard";
import UserOthersDetails from "../../components/userOthersDetails";

const AadhaarEmployeePage = () => {
  const location = useLocation();
  const { t } = useTranslation();
  const { applications, applicationNumber } = location.state || {};
  const { scheme, schemeType, selectedScheme } = location.state || {};
  const tenantId = Digit.ULBService.getCurrentTenantId();

  const handleCallback = useCallback((data) => {
    console.log(data);
  }, []);

  const application = applications?.find((app) => app.applicationNumber === applicationNumber);
  if (!application) {
    return <div>{t("Application not found")}</div>;
  }
  const data = application.ApplicantDetails[0];
  const titleData = data.title.split("_");
  data.title = titleData[titleData.length - 1];

  const checkValue = {};
  checkValue.UserOtherDetails = data.UserOtherDetails;
  checkValue.documentDetails = {};
  checkValue.occupation = data.UserOtherDetails.occupation;
  checkValue.income = data.UserOtherDetails.income;
  checkValue.statement = true;
  checkValue.agreeToPay = true;
  checkValue.employed = true;

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <Title text={t("Applicant Details")} />
        <PersonalDetailCard onUpdate={handleCallback} initialRows={data} tenantId={tenantId} AllowEdit={false} />
        <AddressDetailCard onUpdate={handleCallback} initialRows={data} tenantId={tenantId} AllowEdit={false} />
        <DisabilityCard onUpdate={handleCallback} initialRows={data.divyang} tenantId={tenantId} AllowEdit={false} />
        <DocumentCard
          onUpdate={handleCallback}
          initialRows={data.documentDetails}
          tenantId={tenantId}
          AddOption={false}
          AllowRemove={false}
          AllowEdit={false}
        />
        <UserOthersDetails onUpdate={handleCallback} initialRows={data.UserOtherDetails} tenantId={tenantId} AllowEdit={false} />
        <QualificationCard
          initialRows={data.UserQualification}
          tenantId={tenantId}
          AddOption={false}
          AllowRemove={false}
          onUpdate={handleCallback}
          AllowEdit={false}
        />
        <BankDetails
          onUpdate={handleCallback}
          initialRows={data.UserBank}
          tenantId={tenantId}
          AddOption={false}
          AllowRemove={false}
          AllowEdit={false}
        />

        <SchemeDetailsPage
          onUpdate={handleCallback}
          initialRows={checkValue}
          tenantId={tenantId}
          AllowEdit={false}
          scheme={scheme}
          schemeType={schemeType}
          selectedScheme={selectedScheme}
        />
        <WorkflowActions
          url={"/bmc-service-v1/Employee/workflow/_transit"}
          ActionBarStyle={{}}
          MenuStyle={{}}
          businessService={"bmc-services"}
          applicationNo={applicationNumber}
          tenantId={tenantId}
          moduleCode={"BMC"}
        />
      </div>
    </React.Fragment>
  );
};

export default AadhaarEmployeePage;
