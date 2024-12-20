import React, { Fragment } from "react";
import { Header, Loader } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import FAQComponent from "./FAQComponent";

const FAQsSection = () => {
  const { t } = useTranslation();

  const { data: faqData, isLoading } = Digit.Hooks.useCustomMDMS("mh", "bmc", [{ name: "faqs" }]);

  if (isLoading) {
    return <Loader />;
  }

  return (
    <Fragment>
      <div className="faq-page">
        <div style={{ marginBottom: "15px" }}>
          <Header styles={{ marginLeft: "0px", paddingTop: "10px", fontSize: "36px" }}>{t("BMC_FAQ_S")}</Header>
        </div>

        <div className="faq-list">
          {faqData?.bmc?.faqs?.map((faq, index) => (
            <FAQComponent
              key={index}
              question={faq.question}
              answer={faq.answer}
              index={index + 1}
              lastIndex={index === faqData.bmc.faqs.length - 1}
              subAnswer={faq.subAnswer || []}
              acrynom={faq.acronym || []}
            />
          ))}
        </div>
      </div>
    </Fragment>
  );
};

export default FAQsSection;
