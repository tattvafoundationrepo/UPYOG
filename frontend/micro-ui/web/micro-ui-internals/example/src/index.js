import React from "react";
import ReactDOM from "react-dom";

import { initLibraries } from "@upyog/digit-ui-libraries";

import { initHRMSComponents } from "@upyog/digit-ui-module-hrms";

import { PaymentModule, PaymentLinks, paymentConfigs } from "@upyog/digit-ui-module-common";
import { HRMSModule } from "@upyog/digit-ui-module-hrms";

import { DigitUI } from "@upyog/digit-ui-module-core";

import { BMCLinks, BMCModule, initBMCComponents } from "@tattvafoundation/digit-ui-module-bmc";
import { DEONARModule, initDEONARComponents } from "@tattvafoundation/digit-ui-module-deonar";
import { EBEModule, initEBEComponents } from "@tattvafoundation/digit-ui-module-ebe";


// import {initCustomisationComponents} from "./customisations";

// import { PGRModule, PGRLinks } from "@egovernments/digit-ui-module-pgr";
// import { Body, TopBar } from "@upyog/digit-ui-react-components";
import "@tattvafoundation/upyog-css/dist/index.css";
//import "@upyog-niua/upyog-css/dist/index.css";

// import * as comps from "@upyog/digit-ui-react-components";

// import { subFormRegistry } from "@upyog/digit-ui-libraries";

// import { pgrCustomizations, pgrComponents } from "./pgr";

var Digit = window.Digit || {};

const enabledModules = [
  "Payment",
  "HRMS",
  "BMC",
  "DEONAR",
  "EBE"
];

const initTokens = (stateCode) => {
  const userType = window.sessionStorage.getItem("userType") || process.env.REACT_APP_USER_TYPE || "CITIZEN";

  const token = window.localStorage.getItem("token")|| process.env[`REACT_APP_${userType}_TOKEN`];
 
  const citizenInfo = window.localStorage.getItem("Citizen.user-info")
 
  const citizenTenantId = window.localStorage.getItem("Citizen.tenant-id") || stateCode;

  const employeeInfo = window.localStorage.getItem("Employee.user-info");
  const employeeTenantId = window.localStorage.getItem("Employee.tenant-id");

  const userTypeInfo = userType === "CITIZEN" || userType === "QACT" ? "citizen" : "employee";
  window.Digit.SessionStorage.set("user_type", userTypeInfo);
  window.Digit.SessionStorage.set("userType", userTypeInfo);

  if (userType !== "CITIZEN") {
    window.Digit.SessionStorage.set("User", { access_token: token, info: userType !== "CITIZEN" ? JSON.parse(employeeInfo) : citizenInfo });
  } else {
    // if (!window.Digit.SessionStorage.get("User")?.extraRoleInfo) window.Digit.SessionStorage.set("User", { access_token: token, info: citizenInfo });
  }

  window.Digit.SessionStorage.set("Citizen.tenantId", citizenTenantId);

  if (employeeTenantId && employeeTenantId.length) window.Digit.SessionStorage.set("Employee.tenantId", employeeTenantId);
};

const initDigitUI = () => {
  window?.Digit.ComponentRegistryService.setupRegistry({
    PaymentModule,
    ...paymentConfigs,
    PaymentLinks,
    HRMSModule,
    BMCModule,
    BMCLinks,
    DEONARModule,
    EBEModule
  });

  
  initHRMSComponents();
  initDEONARComponents();
  initBMCComponents();
  initEBEComponents();
  // initCustomisationComponents();

  const moduleReducers = (initData) => ({
  });

  // window.Digit.Customizations = {
  //   PGR: pgrCustomizations,
  //   TL: {
  //     customiseCreateFormData: (formData, licenceObject) => licenceObject,
  //     customiseRenewalCreateFormData: (formData, licenceObject) => licenceObject,
  //     customiseSendbackFormData: (formData, licenceObject) => licenceObject,
  //   },
  // };

  const stateCode = window?.globalConfigs?.getConfig("STATE_LEVEL_TENANT_ID") || "mh";
  initTokens(stateCode);

  const registry = window?.Digit.ComponentRegistryService.getRegistry();
  ReactDOM.render(<DigitUI stateCode={stateCode} enabledModules={enabledModules} moduleReducers={moduleReducers} />, document.getElementById("root"));
};

initLibraries().then(() => {
  initDigitUI();
});
