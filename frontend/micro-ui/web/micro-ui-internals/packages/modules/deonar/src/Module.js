import { AppContainer, Loader } from "@upyog/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";
import { Switch, useRouteMatch } from "react-router-dom";
import DEONARCard from "./components/DeonarCard";
import EmployeeHome from "./pages/employee";
import SecurityCheckPage from "./pages/employee/securityCheck/securityCheck";
import ParkingFeePage from "./pages/employee/securityCheck/parking";
import RemovalFeePage from "./pages/employee/collectionPoint/removalFee";
import RemovalPage from "./pages/employee/securityCheck/removal";
import AssignShopkeeperAfterTrading from "./pages/employee/collectionPoint/assignShopkeeper";
import EntryFeeCollection from "./pages/employee/collectionPoint/entryFee";
import StablingFeePage from "./pages/employee/collectionPoint/stablingFee";
import AnteMortemInspectionPage from "./pages/employee/inspectionPoint/anteMortemInspection";
import SlaughterFeeRecoveryPage from "./pages/employee/slaughterRecoveryPoint/slaughterFeeRecovery";
import VehicleWashing from "./pages/employee/deliveryPoint/vehicleWashing";
import WeighingCharge from "./pages/employee/deliveryPoint/weighingCharge";
import PenaltyCharge from "./pages/employee/deliveryPoint/penaltyCharge";
import GatePass from "./pages/employee/deliveryPoint/gatePass";
import getRootReducer from "./redux/reducers";
import Trading from "./pages/employee/collectionPoint/trading";
import S from "./pages/employee/collectionPoint/s";
import FeeCollection from "./pages/employee/collectionPoint/feeCollection";
import Slaughtering from "./pages/employee/slaughter/slaughtering";
import Helkari from "./pages/employee/collectionPoint/helkari";
import Stakeholder from "./pages/employee/collectionPoint/stakeholder";
import InboxFilter from "./components/InboxFilter";
import DEONARInbox from "./pages/employee/Inbox";
import SlaughteringNormal from "./pages/employee/slaughter/slaughterNormal";
import SlaughteringExport from "./pages/employee/slaughter/slaughterExport";
import SlaughteringEmergency from "./pages/employee/slaughter/slaughterEmergency";
import RemovalReport from "./pages/employee/collectionPoint/RemovalReport";
import WashingFeePage from "./pages/employee/securityCheck/washingFeePage";
import GatePassIssue from "./pages/employee/deliveryPoint/gatePassIssue";
import RegionalGatePass from "./pages/employee/deliveryPoint/regionalGatePass";

export const DEONARModule = ({ stateCode, userType, tenants }) => {
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const language = Digit.StoreData.getCurrentLanguage();
  const { path, url } = useRouteMatch();
  const moduleCode = ["DEONAR"];
  const { isLoading, data: store } = Digit.Services.useStore({
    stateCode,
    moduleCode,
    language,
  });
  Digit.SessionStorage.set("BMC_TENANTS", tenants);

  if (isLoading) {
    return <Loader />;
  }
  return (
    <Switch>
      <AppContainer className="ground-container">
        <EmployeeHome path={path} stateCode={stateCode} userType={userType} tenants={tenants} />
      </AppContainer>
    </Switch>
  );
};

const componentsToRegister = {
  EmployeeHome,
  DEONARModule,
  DEONARCard,
  SecurityCheckPage,
  ParkingFeePage,
  RemovalPage,
  AssignShopkeeperAfterTrading,
  EntryFeeCollection,
  RemovalFeePage,
  StablingFeePage,
  AnteMortemInspectionPage,
  SlaughterFeeRecoveryPage,
  VehicleWashing,
  WeighingCharge,
  PenaltyCharge,
  GatePass,
  Trading,
  FeeCollection,
  S,
  Slaughtering,
  Helkari,
  Stakeholder,
  RegionalGatePass,
  RemovalReport,
  DEONAR_INBOX_FILTER: (props) => <InboxFilter {...props} />,
  DEONARInbox,
  SlaughteringNormal,
  SlaughteringExport,
  SlaughteringEmergency,
  WashingFeePage,
  GatePassIssue,
};

export const initDEONARComponents = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};

export const DEONARReducers = getRootReducer;
