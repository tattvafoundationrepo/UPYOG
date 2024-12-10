import { AppContainer, CitizenHomeCard, Loader } from "@upyog/digit-ui-react-components";
import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Switch, useRouteMatch } from "react-router-dom";

//Citizen Pages
import WorkflowPopup from "./components/Workflowpopup";


//Employee Pages

import WorkflowActions from "./components/Workflow";
import EBEEmployeeHome from "./pages/employee";

//Master Pages

import getRootReducer from "./redux/reducers";
import PreEnquiryMain from "./pages/employee/PreEnquiryMain";
import PreEnquiryCard from "./components/PreEnquiry";
import EBECard from "./components/EBECard";
import EmployeeDetails from "./components/EmployeeDetails";
import TestCard from "./components/modal/Testcard";
import ActionComment from "./components/modal/ActionComment";
import ActionComment1 from "./components/modal/ActionComment1";
import ReportSubmission from "./components/modal/ReportSubmission";
import PEList from "./pages/employee/PEList";
import DEList from "./pages/employee/DEList";
import PreDetail from "./pages/employee/PEDetail";
import PEUpdateDetail from "./pages/employee/PEUpdateDetail";
import EmployeeAdd from "./components/EmployeeAdd";
import DEDetail from "./pages/employee/DEDetail";
import StatusTags from "./components/StatusTags";
import  MultiColumnDropdown  from "./components/MultiColumnDropdown";
import ExampleDropdownUsage from "./pages/employee/test";

export const EBEModule = ({ stateCode, userType, tenants }) => {
  const tenantId = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.code || Digit.ULBService.getCurrentTenantId();
  const language = Digit.StoreData.getCurrentLanguage();
  const { path, url } = useRouteMatch();
  const moduleCode = ["EBE"];
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
    return null;
  }

  if (userType === "employee") {
  return (<Switch>
    <AppContainer className="ground-container">
      <EBEEmployeeHome path={path} stateCode={stateCode} />
    </AppContainer>
  </Switch>);
  }
};

export const EBELinks = ({ matchPath }) => {
  const { t } = useTranslation();
  const [params, setParams, clearParams] = Digit.Hooks.useSessionStorage(BMC_CITIZEN_CREATE_COMPLAINT, {});

  useEffect(() => {
    clearParams();
  }, []);

  const links = [
    {
      link: `${matchPath}/create`,
      i18nKey: t("CS_CREATE"),
    },
  ];

  return <CitizenHomeCard header={t("CS_COMMON_HOME_COMPLAINTS")} links={links} />;
};

const componentsToRegister = {
  EBEEmployeeHome,
  EBEModule,
  EBELinks,
  EBEWorkFlow:WorkflowActions,
  WorkflowPopup,
  PreEnquiryMain,
  PreEnquiryCard,
  EBECard,
  EmployeeDetails,
  TestCard,
  ActionComment,
  ReportSubmission,
  PEList,
  DEList,
  PreDetail,
  PEUpdateDetail,
  ActionComment1,
  EmployeeAdd,
  DEDetail,
  StatusTags,
  MultiColumnDropdown,
  ExampleDropdownUsage
};

export const initEBEComponents = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};
export const EBEReducers = getRootReducer;