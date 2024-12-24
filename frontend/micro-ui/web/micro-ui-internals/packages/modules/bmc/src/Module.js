import { CitizenHomeCard, Loader, PTIcon } from "@upyog/digit-ui-react-components";
import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useRouteMatch } from "react-router-dom";

//Citizen Pages
import AddressDetailCard from "./components/AddressDetails";
import DisabilityCard from "./components/DisabilityCard";
import PersonalDetailCard from "./components/PersonalDetails";
import QualificationCard from "./components/QualificationCard";
import WorkflowPopup from "./components/Workflowpopup";
import AadhaarVerification from "./pagecomponents/aadhaarVerification";
import AadhaarFullForm from "./pagecomponents/aadhaarfullformpge";
import ApplicationDetail from "./pagecomponents/applicationDetail";
import BMCReviewPage from "./pagecomponents/bmcReview";
import SelectSchemePage from "./pagecomponents/selectScheme";
import BMCCitizenHome from "./pages/citizen";
import SchemeDetailsPage from "./components/schemeDetails";
import FAQsSection from "./pages/FAQS/FAQs";
import HowItsWorkSection from "./pages/HowItWork/HowItwork";

//Employee Pages
import BMCCard from "./components/BMCCard";
import InboxFilter from "./components/InboxFilter";
import SearchApplications from "./components/SearchApplications";

import WorkflowActions from "./components/Workflow";
import BMCEmployeeHome from "./pages/employee";
import ApprovePage from "./pages/employee/Approve";
import BMCInbox from "./pages/employee/Inbox";
import AadhaarEmployeePage from "./pages/employee/aadhaarEmployee";
import AadhaarVerifyPage from "./pages/employee/aadhaarVerify";
import RandmizationPage from "./pages/employee/randmization";
//Master Pages

import getRootReducer from "./redux/reducers";
import DocumentCard from "./components/DocumentCard";
import UserOthersDetails from "./components/userOthersDetails";
import BankDetailsForm from "./components/BankDetails";
import AllApplicationsPage from "./components/AllApplications";
import CurrentSchemeApplications from "./components/currentScheme";

export const BMCModule = ({ stateCode, userType, tenants }) => {
  const tenantId = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.code || Digit.ULBService.getCurrentTenantId();
  const language = Digit.StoreData.getCurrentLanguage();
  const { path, url } = useRouteMatch();
  const moduleCode = ["BMC"];
  const { isLoading, data: store } = Digit.Services.useStore({
    stateCode,
    moduleCode,
    language,
  });
  Digit.SessionStorage.set("BMC_TENANTS", tenants);

  if (isLoading) {
    return <Loader />;
  }
  if (userType === "citizen") {
    return (
      <BMCCitizenHome path={path} stateCode={stateCode} />
    );
  }
  return (
    <BMCEmployeeHome path={path} stateCode={stateCode} />
  );
};

export const BMCLinks = ({ matchPath,userType }) => {
  const { t } = useTranslation();
  const [params, setParams, clearParams] = Digit.Hooks.useSessionStorage("PT_CREATE_PROPERTY", {});

  useEffect(() => {
    clearParams();
  }, []);

  const links = [
    {
      link: `/digit-ui/citizen/bmc/aadhaarLogin`,
      i18nKey: t("BMC_NEWAPPLICATION"),
    },
    {
      link: `/digit-ui/citizen/bmc/allApplications`,
      i18nKey: t("BMC_MYAPPLICATIONS"),
    },
    {
      link: `/digit-ui/citizen/bmc/howItWorks`,
      i18nKey: t("BMC_HOW_IT_WORKS"),
    },
    {
      link: `/digit-ui/citizen/bmc/faqs`,
      i18nKey: t("BMC_FAQ_S"),
    },
    {
      link: `/digit-ui/citizen/bmc/avilablescheme`,
      i18nKey: t("BMC_CURRENTSCHEME"),
    }
  ];

  return (
    <React.Fragment>
      <div className="HomePageWrapper" style={{"padding-top": 10}}>
        <div className="BannerWithSearch">
        <img src={"https://gambia-terraform.s3.ap-south-1.amazonaws.com/Free-Silai-Machine-Yojna-1-jpg.jpeg"} alt="noimagefound" />
        </div>
        <CitizenHomeCard header={t("BMC")} links={links} Icon={() => <PTIcon className="fill-path-primary-main" />} />
      </div>
    </React.Fragment>
  );
};

const componentsToRegister = {
  BMCCitizenHome,
  BMCEmployeeHome,
  BMCModule,
  BMCLinks,
  ApplicationDetail,
  AadhaarVerification,
  AadhaarFullForm,
  SelectSchemePage,
  BMCReviewPage,
  AadhaarEmployeePage,
  RandmizationPage,
  AadhaarVerifyPage,
  ApprovePage,
  BMCCard,
  BMCInbox,
  QualificationCard,
  DisabilityCard,
  PersonalDetailCard,
  AddressDetailCard,
  WorkflowActions,
  WorkflowPopup,
  SearchApplications,
  SchemeDetailsPage,
  BMC_INBOX_FILTER: (props) => <InboxFilter {...props} />,
  DocumentCard,
  UserOthersDetails,
  BankDetailsForm,
  AllApplicationsPage,
  CurrentSchemeApplications,
  FAQsSection,
  HowItsWorkSection
};

export const initBMCComponents = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};
export const BMCReducers = getRootReducer;
