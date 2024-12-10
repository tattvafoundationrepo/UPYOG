export const newConfig = (data) => {
  let arr = []
  switch (data.replace("Action: ", "")) {
    case "INITIATE":
      arr = [
        {
          body: [
            {
              type: "component",
              component: "ActionComment",
              key: "ActionComment",
              withoutLabel: true,
            },

          ],
        }
      ];
      break;
    case 'CASE_ASSESSMENT':
      arr = [
        {
          body: [
            {
              type: "component",
              component: "TestCard",
              key: "TestCard",
              withoutLabel: true,
            },
            {
              type: "component",
              component: "ActionComment",
              key: "ActionComment",
              withoutLabel: true,
            }
          ],
        }
      ];
      break;
    case 'EVIDENCE_RECORDING':
      arr = [
        {
          body: [
            {
              type: "component",
              component: "TestCard",
              key: "TestCard",
              withoutLabel: true,
            },
            {
              type: "component",
              component: "ActionComment",
              key: "ActionComment",
              withoutLabel: true,
            }
          ],
        }
      ];
      break;
    case 'APPROVE':
      arr = [
        {
          body: [
            {
              type: "component",
              component: "ReportSubmission",
              key: "ReportSubmission",
              withoutLabel: true,
            },
            {
              type: "component",
              component: "ActionComment",
              key: "ActionComment",
              withoutLabel: true,
            },
          ],
        }
      ];
      break;
    default:
      arr = [
        {
          body: [
            {
              type: "component",
              component: "TestCard",
              key: "TestCard",
              withoutLabel: true,
            },
            {
              type: "component",
              component: "ActionComment",
              key: "ActionComment",
              withoutLabel: true,
            }
          ],
        }
      ];
      break;
  }
  return arr;
};
