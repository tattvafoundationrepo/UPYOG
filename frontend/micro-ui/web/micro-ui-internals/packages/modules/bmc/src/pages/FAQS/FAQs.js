// import { Header, TextInput, ArrowForward, Loader, BackButton } from "@upyog/digit-ui-react-components";
// import React, { useState, Fragment, useRef, useEffect } from "react";
// import { useTranslation } from "react-i18next";
// import FAQComponent from "./FAQComponent";
// import FAQsJson from "./FAQs.json";

// const FAQsSection = () => {
//   const user = Digit.UserService.getUser();
//   const tenantId = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.code || Digit.ULBService.getCurrentTenantId();
//   const { t } = useTranslation();
//   const { isLoading, data } = Digit.Hooks.useGetBMCFAQsJSON("mh.mumbai");
//   const moduleFAQs = data?.MdmsRes["BMC"]?.FAQs[0]?.[`BMC`].FAQs;

//   console.log("moduleFAQs", data);

//   if (isLoading) {
//     return <Loader />;
//   }
//   return (
//     <Fragment>
//       <div className="faq-page">
//         <div style={{ marginBottom: "15px" }}>
//           <Header styles={{ marginLeft: "0px", paddingTop: "10px", fontSize: "36px" }}>{t("WelFare-Scheme_FAQS")}</Header>
//         </div>

//         <div className="faq-list">
//           {moduleFAQs?.map((faq, i) => (
//             <FAQComponent key={"FAQ" + i} question={faq.question} acrynom={faq.acrynom} answer={faq.answer} subAnswer={faq.subAnswer} index={i + 1} />
//           ))}
//         </div>
//       </div>
//     </Fragment>
//   );
// };

// export default FAQsSection;

import React, { Fragment } from "react";
import { Header, Loader } from "@upyog/digit-ui-react-components";
import FAQComponent from "./FAQComponent";
import FAQsJson from "./FAQs.json"; // Importing the JSON file
import { useTranslation } from "react-i18next";

const FAQsSection = () => {
  const { t } = useTranslation();
  const { FAQs } = FAQsJson; // Destructure FAQs from the imported JSON data

  if (!FAQs || FAQs.length === 0) {
    return <Loader />;
  }

  return (
    <Fragment>
      <div className="faq-page">
        <div style={{ marginBottom: "15px" }}>
          <Header styles={{ marginLeft: "0px", paddingTop: "10px", fontSize: "36px" }}>{t("BMC_FAQ_S")}</Header>
        </div>

        <div className="faq-list">
          {FAQs[0].BMC.FAQs.map((faq, i) => (
            <FAQComponent
              key={"FAQ" + i}
              question={faq.question}
              acrynom={faq.acrynom}
              answer={faq.answer}
              subAnswer={faq.subAnswer}
              index={i + 1}
              lastIndex={i === FAQs[0].BMC.FAQs.length - 1}
            />
          ))}
        </div>
      </div>
    </Fragment>
  );
};

export default FAQsSection;
