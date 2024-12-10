import { AppContainer, BreadCrumb, PrivateRoute } from "@upyog/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";
import { Switch, useLocation, useRouteMatch } from "react-router-dom";


const App = ({ path, stateCode, userType, tenants }) => {
  const { t } = useTranslation();
  const location = useLocation();
  const match = useRouteMatch();
  // const AadhaarSatutsVerificationPage = Digit?.ComponentRegistryService?.getComponent("AadhaarSatutsVerificationPage");
  const PreEnquiryMain = Digit?.ComponentRegistryService?.getComponent("PreEnquiryMain");
  const PreEnquiryList = Digit?.ComponentRegistryService?.getComponent("PEList");
  const DEnquiryList = Digit?.ComponentRegistryService?.getComponent("DEList");
  const PEDetail = Digit.ComponentRegistryService?.getComponent("PEUpdateDetail");
  const test = Digit.ComponentRegistryService?.getComponent("ExampleDropdownUsage");
  
  //const AadhaarVerifyPage = Digit.ComponentRegistryService?.getComponent("AadhaarVerifyPage");
  const ApprovePage = Digit.ComponentRegistryService?.getComponent("ApprovePage");
  const mobileView = innerWidth <= 640;
  const tenantId = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.code || Digit.ULBService.getCurrentTenantId();
  const Inbox = Digit?.ComponentRegistryService?.getComponent('BMCInbox');
  const inboxInitialState = {
    searchParams: {
      tenantId: tenantId,
    },
  };
  const ProjectBreadCrumb = ({ location }) => {
    const { t } = useTranslation();
    const crumbs = [
      {
        path: `/digit-ui/employee`,
        content: t("HOME"),
        show: true,
      },
      {
        path: `/digit-ui/employee`,
        content: t(location.pathname.split("/").pop()),
        show: true,
      },
    ];
    return <BreadCrumb crumbs={crumbs} spanStyle={{ maxWidth: "min-content" }} />;
  };
  return (
    <Switch>
      <AppContainer className="ground-container">
        <React.Fragment>
          <ProjectBreadCrumb location={location} />
        </React.Fragment>
        <PrivateRoute path={`${path}/inbox`} component={() => (
              <Inbox parentRoute={path} businessService="ebe" filterComponent="BMC_INBOX_FILTER" initialStates={inboxInitialState} isInbox={true} />
            )} />
        <PrivateRoute path={`${path}/PECreate`} component={PreEnquiryMain} />
        <PrivateRoute path={`${path}/PEList`} component={PreEnquiryList} />
        <PrivateRoute path={`${path}/DEList`} component={DEnquiryList} />
        <PrivateRoute path={`${path}/PEUpdate/:type/:id`} component={PEDetail} />
        <PrivateRoute path={`${path}/test`} component={test} />
      </AppContainer>
    </Switch>
  );
};

export default App;