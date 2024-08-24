import React from "react";


import {
  PaymentModule,
  PaymentLinks,
  paymentConfigs,
} from "@upyog/digit-ui-module-common";
import { DigitUI } from "@upyog/digit-ui-module-core";
import { initLibraries } from "@upyog/digit-ui-libraries";
import {
  HRMSModule,
  initHRMSComponents,
} from "@upyog/digit-ui-module-hrms";


import { initCustomisationComponents } from "./Customisations";

import { BMCLinks, BMCModule, initBMCComponents } from "@tattvafoundation/digit-ui-module-bmc";
import { DEONARModule, initDEONARComponents } from "@tattvafoundation/digit-ui-module-deonar";
// import { initReportsComponents } from "@egovernments/digit-ui-module-reports";

initLibraries();

const enabledModules = [
  "Payment",
  "HRMS",
  "BMC",
  "DEONAR"
];
window.Digit.ComponentRegistryService.setupRegistry({
  ...paymentConfigs,
  PaymentModule,
  PaymentLinks,
  HRMSModule,
  BMCLinks,
  BMCModule,
  DEONARModule
});



initHRMSComponents();
initDEONARComponents();
initBMCComponents();
// initReportsComponents();
initCustomisationComponents();

const moduleReducers = (initData) => ({
 
});

function App() {
  const stateCode =
    window.globalConfigs?.getConfig("STATE_LEVEL_TENANT_ID") ||
    process.env.REACT_APP_STATE_LEVEL_TENANT_ID;
  if (!stateCode) {
    return <h1>stateCode is not defined</h1>;
  }
  return (
    <DigitUI
      stateCode={stateCode}
      enabledModules={enabledModules}
      moduleReducers={moduleReducers}
    />
  );
}

export default App;
