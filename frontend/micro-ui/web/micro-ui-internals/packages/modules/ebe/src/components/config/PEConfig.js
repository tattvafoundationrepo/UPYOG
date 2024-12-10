export const PEConfig = (data)=>{
  let arr=[]
  switch(data.replace("Action: ", "")){
    case 'UPDATE': 
    arr=[
      {
        body: [
          {
            type: "component",
            component: "EmployeeAdd",
            key: "EmployeeAdd",
            withoutLabel: true,
          }
        ],
      }
    ];
      break;
    case 'DE':
      arr=[
        {
          body: [
            {
              type: "component",
              component: "DEDetail",
              key: "DEDetail",
              withoutLabel: true,
            }
          ],
        }
      ];
      break;
      case 'PE':
        arr=[
          {
            body: [
              {
                type: "component",
                component: "PreDetail",
                key: "PreDetail",
                withoutLabel: true,
              }
            ],
          }
        ];
        break;
    default:
      arr=[
        {
          body: [
            {
              type: "component",
              component: "PreDetail",
              key: "PreDetail",
              withoutLabel: true,
            },
           
          ],
        }
      ];
      break;
  }
  return arr;
};