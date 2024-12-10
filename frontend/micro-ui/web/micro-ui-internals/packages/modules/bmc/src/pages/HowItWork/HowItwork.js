import React, { Fragment } from "react";
import { Header, Loader } from "@upyog/digit-ui-react-components";
import HowItWorkComponent from "./HowItWorkComponent";
import { useTranslation } from "react-i18next";
import howItWorkssJson from "./HowItWork.json"; // Importing the JSON file

const HowItsWorkSection = () => {
  const { t } = useTranslation();
  const { FAQs } = howItWorkssJson; // Destructure FAQs from the imported JSON data

  if (!FAQs || FAQs.length === 0) {
    return <Loader />;
  }

  return (
    <Fragment>
      <div className="faq-page">
        <div style={{ marginBottom: "15px" }}>
          <Header styles={{ marginLeft: "0px", paddingTop: "10px", fontSize: "36px" }}>{t("BMC_HOW_IT_WORKS")}</Header>
        </div>

        <div className="faq-list">
          {FAQs[0].BMC.FAQs.map((faq, i) => (
            <HowItWorkComponent
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

export default HowItsWorkSection;
