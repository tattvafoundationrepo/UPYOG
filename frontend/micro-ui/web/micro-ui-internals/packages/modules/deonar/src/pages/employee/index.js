import { AppContainer, BreadCrumb, PrivateRoute } from "@upyog/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";
import { Switch, useLocation, useRouteMatch } from "react-router-dom";

const App = ({ path, stateCode, userType, tenants }) => {
  const { t } = useTranslation();
  const location = useLocation();
  const match = useRouteMatch();
  const SecurityCheckPage = Digit.ComponentRegistryService?.getComponent("SecurityCheckPage");
  const ParkingFeePage = Digit.ComponentRegistryService?.getComponent("ParkingFeePage");
  const RemovalPage = Digit.ComponentRegistryService?.getComponent("RemovalPage");
  const AssignShopkeeperAfterTradingPage = Digit.ComponentRegistryService?.getComponent("AssignShopkeeperAfterTrading");
  const EntryFeePage = Digit.ComponentRegistryService?.getComponent("EntryFeeCollection");
  const RemovalFeePage = Digit.ComponentRegistryService?.getComponent("RemovalFeePage");
  const StablingFeePage = Digit.ComponentRegistryService?.getComponent("StablingFeePage");
  const AnteMortemInspectionPage = Digit.ComponentRegistryService?.getComponent("AnteMortemInspectionPage");
  const SlaughterFeeRecoveryPage = Digit.ComponentRegistryService?.getComponent("SlaughterFeeRecoveryPage");
  const VehicleWashing = Digit.ComponentRegistryService?.getComponent("VehicleWashing");
  const WeighingCharge = Digit.ComponentRegistryService?.getComponent("WeighingCharge");
  const PenaltyCharge = Digit.ComponentRegistryService?.getComponent("PenaltyCharge");
  const GatePass = Digit.ComponentRegistryService?.getComponent("GatePass");
  const Trading = Digit.ComponentRegistryService?.getComponent("Trading");
  const S = Digit.ComponentRegistryService?.getComponent("S");
  const FeeCollection = Digit.ComponentRegistryService?.getComponent("FeeCollection");
  const Slaughtering = Digit.ComponentRegistryService?.getComponent("Slaughtering");
  const Helkari = Digit.ComponentRegistryService?.getComponent("Helkari");
  const Stakeholder = Digit.ComponentRegistryService?.getComponent("Stakeholder");
  const Inbox = Digit?.ComponentRegistryService?.getComponent('DEONARInbox');
  const mobileView = innerWidth <= 640;
  const tenantId = Digit.ULBService.getCurrentTenantId();
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
        <PrivateRoute path={`${path}/securitycheck`} component={SecurityCheckPage} />
        <PrivateRoute path={`${path}/parking`} component={ParkingFeePage} />
        <PrivateRoute path={`${path}/removal`} component={RemovalPage} />
        <PrivateRoute path={`${path}/assignshopkeeper`} component={AssignShopkeeperAfterTradingPage} />
        <PrivateRoute path={`${path}/entryfee`} component={EntryFeePage} />
        <PrivateRoute path={`${path}/removalfee`} component={RemovalFeePage} />
        <PrivateRoute path={`${path}/stabling`} component={StablingFeePage} />
        <PrivateRoute path={`${path}/trading`} component={Trading} />
        <PrivateRoute path={`${path}/s`} component={S} />
        <PrivateRoute path={`${path}/feeCollection`} component={FeeCollection} />
        <PrivateRoute path={`${path}/slaughtering`} component={Slaughtering} />
        <PrivateRoute path={`${path}/inspection`} component={AnteMortemInspectionPage} />
        <PrivateRoute path={`${path}/slaughterfeerecovery`} component={SlaughterFeeRecoveryPage} />
        <PrivateRoute path={`${path}/vehiclewashing`} component={VehicleWashing} />
        <PrivateRoute path={`${path}/weighingcharge`} component={WeighingCharge} />
        <PrivateRoute path={`${path}/penaltyCharge`} component={PenaltyCharge} />
        <PrivateRoute path={`${path}/gatePass`} component={GatePass} />
        <PrivateRoute path={`${path}/stakeholder`} component={Stakeholder} />
        <PrivateRoute path={`${path}/helkari`} component={Helkari} />
        <PrivateRoute path={`${path}/inbox`} component={() => (
              <Inbox parentRoute={path} businessService="deonar" filterComponent="DEONAR_INBOX_FILTER" initialStates={inboxInitialState} isInbox={true} />
            )} />
      </AppContainer>
    </Switch>
  );
};

export default App;
